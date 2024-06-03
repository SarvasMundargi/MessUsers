package com.example.auntymess

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.auntymess.Models.UserData
import com.example.auntymess.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private val PICK_IMAGE_REQEST=1
    private var imageUri: Uri?=null

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()

        val action=intent.getStringExtra("action")
        val MessName=intent.getStringExtra("messName")
        binding.buttonSelectMess.text=MessName

        if(action=="login"){
            binding.loginEmail.visibility= View.VISIBLE
            binding.loginPassword.visibility= View.VISIBLE
            //binding.tvNewhere.visibility= View.INVISIBLE
            binding.buttonRegister.visibility= View.INVISIBLE
            binding.registerEmail.visibility= View.GONE
            binding.registerName.visibility= View.GONE
            binding.registerPassword.visibility= View.GONE
            binding.cardView.visibility= View.GONE

            //Handling login

            binding.buttonLogin.setOnClickListener {
                val email=binding.loginEmail.text.toString()
                val password=binding.loginPassword.text.toString()

                if(email.isBlank() || password.isBlank()){
                    Toast.makeText(this,"Please fill all the Credentials", Toast.LENGTH_SHORT).show()
                }
                else{
                    auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this){
                        if(it.isSuccessful){
                            Toast.makeText(this,"Logged In😁", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this,MainActivity::class.java))
                            finish()
                        }
                        else{
                            Toast.makeText(this,"Not proper credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        }

        else if(action=="register"){
            binding.buttonLogin.isEnabled=false
            binding.buttonLogin.alpha=0.5f

            binding.buttonRegister.setOnClickListener {
                val email=binding.registerEmail.text.toString()
                val password=binding.registerPassword.text.toString()
                val name=binding.registerName.text.toString()

                if(email.isBlank() || password.isBlank() || name.isBlank()){
                    Toast.makeText(this,"Please fill all the Credentials", Toast.LENGTH_SHORT).show()
                }
                else{
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                        if(it.isSuccessful){
                            //auth.signOut()
                            val user=auth.currentUser
                            //Adding the user Database
                            user?.let {
                                addUserData(name,email,auth.currentUser!!.uid)
                                Toast.makeText(this,"Logged In",Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this,MainActivity::class.java))
                                finish()
                            }
                        }
                        else{
                            Toast.makeText(this,"Error Connecting User",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding.cardView.setOnClickListener{
            val intent=Intent()
            intent.type="image/*"
            intent.action=Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent,"select image"),
                PICK_IMAGE_REQEST
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==PICK_IMAGE_REQEST && resultCode== RESULT_OK && data!=null && data.data!=null)
            imageUri=data.data
        Glide.with(this).load(imageUri).apply(RequestOptions.circleCropTransform())
            .into(binding.registerImage)
    }

    private fun addUserData(name: String, email: String, uid: String) {
        val database = FirebaseDatabase.getInstance().getReference()
        val messName = intent.getStringExtra("messName")
        binding.buttonSelectMess.text = messName  // Update UI with messName

        getMessId(messName) { messId ->
            if (messId != null) {
                // Store user data under "MessOwners/{messId}/users/{uid}"
                val userData = UserData(uid, name, email)
                database.child("MessOwners").child(messId).child("users").child(uid).setValue(userData)

                // Upload profile image to Firebase Storage
                val storageRef = FirebaseStorage.getInstance().getReference()
                val imageRef = storageRef.child("profile_img/$uid.jpg")

                imageRef.putFile(imageUri!!)
                    .addOnSuccessListener { _ ->
                        // Get the download URL of the uploaded image
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()

                            // Store the image URL under user node in database
                            database.child("MessOwners").child(messId).child("users").child(uid)
                                .child("profileImage").setValue(imageUrl)

                            // Handle image upload success
                            Log.d("Firebase", "Image uploaded successfully")
                        }.addOnFailureListener { exception ->
                            // Handle failure to get image URL
                            Log.e("Firebase", "Error getting download URL", exception)
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure to upload image
                        Log.e("Firebase", "Error uploading image", exception)
                    }
            } else {
                Log.e("Firebase", "MessId not found for messName: $messName")
            }
        }
    }


    private fun getMessId(messName: String?, callback: (String?) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference()

        database.child("MessOwners").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var messId: String? = null

                for (messSnapshot in snapshot.children) {
                    val name = messSnapshot.child("MessName").getValue(String::class.java)

                    if (name != null && name == messName) {
                        messId = messSnapshot.key
                        break
                    }
                }

                callback.invoke(messId)  // Invoke the callback with the retrieved messId
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled if needed
                callback.invoke(null)  // Invoke callback with null in case of error
            }
        })
    }
}