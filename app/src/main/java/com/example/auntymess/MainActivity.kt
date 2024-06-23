package com.example.auntymess

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.auntymess.Models.AttendanceItemModel
import com.example.auntymess.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var toggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference()
        binding.pgBarPresent.visibility= View.VISIBLE
        binding.pgBarAbsent.visibility= View.VISIBLE
        binding.presentCount.visibility=View.GONE
        binding.absentCount.visibility=View.GONE

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener{
            when(it.itemId){
                R.id.Logout->{
                    auth.signOut()
                    startActivity(Intent(this,WelcomeActivity::class.java))
                    Toast.makeText(applicationContext,"Logged Out", Toast.LENGTH_SHORT).show()
                    finish()
                }
                R.id.nav_home->{
                    drawerLayout.close()
                }
                R.id.profile->{
                    startActivity(Intent(this,ProfileActivity::class.java))
                }
            }
            true
        }

        val uid = auth.currentUser?.uid

        binding.checkBalance.setOnClickListener {
            getMessId(uid) { messId ->
                if (messId != null) {
                    val intent = Intent(this, BalanceActivity::class.java)
                    intent.putExtra("messId", messId)
                    startActivity(intent)
                }
            }
        }

        binding.presentdatesBtn.setOnClickListener {
            getMessId(uid) { messId ->
                if (messId != null) {
                    val intent = Intent(this, PresentAbsentActivity::class.java)
                    intent.putExtra("activity", "Main")
                    intent.putExtra("action", "present")
                    intent.putExtra("messId", messId)
                    startActivity(intent)
                }
            }
        }

        binding.absentdatesBtn.setOnClickListener {
            getMessId(uid) { messId ->
                if (messId != null) {
                    val intent = Intent(this, PresentAbsentActivity::class.java)
                    intent.putExtra("activity", "Main")
                    intent.putExtra("action", "absent")
                    intent.putExtra("messId", messId)
                    startActivity(intent)
                }
            }
        }

        if (uid != null) {
            getMessId(uid) { messId ->
                if (messId != null) {
                    val attRef = database.child("MessOwners").child(messId).child("attendance").child(uid)
                    attRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val attModel = snapshot.getValue(AttendanceItemModel::class.java)
                                if (attModel != null) {
                                    binding.presentCount.text = attModel.presentCount.toString()
                                    binding.absentCount.text = attModel.absentCount.toString()
                                    binding.messStartDate.text = attModel.startDate
                                    binding.messEndDate.text = attModel.endDate
                                }
                            }
                            binding.pgBarPresent.visibility= View.GONE
                            binding.pgBarAbsent.visibility= View.GONE
                            binding.presentCount.visibility=View.VISIBLE
                            binding.absentCount.visibility=View.VISIBLE
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error if needed
                        }
                    })
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
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
}
