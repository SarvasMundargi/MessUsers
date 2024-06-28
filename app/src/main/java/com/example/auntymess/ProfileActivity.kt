package com.example.auntymess

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
import com.example.auntymess.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private val PICK_IMAGE_REQEST=1
    private var imageUri: Uri?=null
    private var flag: Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference()

        val userid = auth.currentUser?.uid

        // Displaying previous name and the photo
        if (userid != null) {
            getMessId(userid) { messid ->
                if (messid != null) {
                    database.child("MessOwners").child(messid).child("users").child(userid)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val userdata = snapshot.getValue(UserData::class.java)

                                    if (userdata != null) {
                                        binding.editProfilename.text = userdata.name

                                        // Ensure activity is not destroyed before loading image with Glide
                                        if (!isDestroyed) {
                                            Glide.with(this@ProfileActivity)
                                                .load(userdata.profileImage)
                                                .into(binding.registerImage)
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle onCancelled
                            }
                        })
                }
            }
        }

        // Handling change profile picture button
        binding.changepicButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select image"),
                PICK_IMAGE_REQEST
            )
        }

        binding.updateProfileButton.setOnClickListener {
            val newname = binding.registerName.text.toString()
            var changeMade = false


            if (newname.isBlank() && !flag) {
                Toast.makeText(this, "No Changes Made", Toast.LENGTH_SHORT).show()
            } else {
                binding.updateProfileButton.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                Log.d("TAG", "Button clicked")
                if (newname.isNotBlank()) {
                    changeMade = true
                    getMessId(userid) { messid ->
                        if (messid != null && userid != null) {
                            database.child("MessOwners").child(messid).child("users").child(userid).child("name")
                                .setValue(newname)
                            binding.updateProfileButton.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }

                if (flag) { // Assuming flag indicates if the profile picture was updated
                    changeMade = true
                    // Update the profile image in the database after getting URL from storage
                    imageUri?.let { uri ->
                        uploadProfileImage(uri) { downloadUri ->
                            if (downloadUri != null) {
                                getMessId(userid) { messid ->
                                    if (messid != null && userid != null) {
                                        val newProfileImageUrl = downloadUri.toString()
                                        database.child("MessOwners").child(messid).child("users").child(userid).child("profileImage")
                                            .setValue(newProfileImageUrl)
                                        binding.updateProfileButton.visibility = View.VISIBLE
                                        binding.progressBar.visibility = View.GONE
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            if (changeMade) {
                Toast.makeText(this, "Changes Updated", Toast.LENGTH_SHORT).show()
                flag = false
                binding.registerName.text.clear()
                binding.registerName.clearFocus()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQEST && resultCode == RESULT_OK && data != null && data.data != null){
            imageUri = data.data
            Glide.with(this).load(imageUri).apply(RequestOptions.circleCropTransform())
                .into(binding.registerImage)

            flag=true
        }

    }

    override fun onBackPressed() {
        // Check if progress bar is visible, if so, do nothing
        if (binding.progressBar.visibility == View.VISIBLE) {
            return
        }
        super.onBackPressed()
    }

    private fun getMessId(uid: String?, callback: (String?) -> Unit) {
        if (uid == null) {
            callback(null)
            return
        }

        val messOwnersRef = database.child("MessOwners")
        messOwnersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var messId: String? = null
                for (messSnapshot in snapshot.children) {
                    if (messSnapshot.exists()) {
                        val id = messSnapshot.key
                        val present = messSnapshot.child("users").child(uid).exists()
                        if (present) {
                            messId = id
                            break
                        }
                    }
                }
                callback(messId)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    private fun uploadProfileImage(imageUri: Uri, callback: (Uri?) -> Unit) {
        val userid = auth.currentUser!!.uid
        val storageReference = FirebaseStorage.getInstance().reference.child("profile_img").child("$userid.jpg")
        val uploadTask = storageReference.putFile(imageUri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(task.result)
            } else {
                callback(null)
            }
        }
    }
}
