package com.example.auntymess.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.auntymess.LoginActivity
import com.example.auntymess.MapActivity
import com.example.auntymess.databinding.MessItemBinding
import com.google.firebase.database.FirebaseDatabase

class StudentAdapter(private val items: MutableList<String>): RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MessItemBinding.inflate(inflater, parent, false)
        return StudentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val stuitem = items[position]
        holder.bind(stuitem)
    }

    inner class StudentViewHolder(private val binding: MessItemBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(stuitem: String) {
            binding.profileName.text = stuitem

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, LoginActivity::class.java)
                intent.putExtra("messName", stuitem)
                intent.putExtra("action", "register")
                context.startActivity(intent)

                if (context is Activity) {
                    context.finish()
                }
            }

            binding.root.setOnLongClickListener {
                // Traverse Firebase to find matching mess name
                val database = FirebaseDatabase.getInstance().getReference()
                database.child("MessOwners").get().addOnSuccessListener { snapshot ->
                    var locationFound = false
                    for (messSnapshot in snapshot.children) {
                        val messName = messSnapshot.child("MessName").value.toString()
                        if (messName == stuitem) {
                            val latitude = messSnapshot.child("Latitude").value.toString()
                            val longitude = messSnapshot.child("longitude").value.toString()
                            launchMapActivity(binding.root.context, latitude, longitude,stuitem)
                            Log.d("TAG", "$latitude and $longitude")
                            locationFound = true
                            break
                        }
                    }
                    if (!locationFound) {

                        Toast.makeText(
                            binding.root.context,
                            "Location not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener {
                    // Handle failure (e.g., data retrieval error)
                    Toast.makeText(
                        binding.root.context,
                        "Error retrieving location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true // Consume long click event
            }
        }
    }

    // Function to launch MapActivity with latitude and longitude
    private fun launchMapActivity(
        context: Context,
        latitude: String,
        longitude: String,
        stuitem: String
    ) {
        val intent = Intent(context, MapActivity::class.java).apply {
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("messName",stuitem)
        }
        context.startActivity(intent)
    }
}