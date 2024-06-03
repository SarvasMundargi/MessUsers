package com.example.auntymess


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auntymess.Adapters.StudentAdapter

import com.example.auntymess.databinding.ActivitySelectMessBinding
import com.example.auntymess.databinding.MessItemBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SelectMessActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectMessBinding
    private lateinit var messlist: MutableList<String>
    private lateinit var databaseRef: DatabaseReference
    private lateinit var mAdapter: StudentAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySelectMessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef=FirebaseDatabase.getInstance().getReference()
        messlist= mutableListOf()

        mAdapter= StudentAdapter(messlist)
        val recyclerView=binding.messListRecyclerview
        recyclerView.layoutManager=LinearLayoutManager(this)
        recyclerView.adapter=mAdapter


        databaseRef.child("MessOwners").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messlist.clear()
                for(messSnapshot in snapshot.children){
                    val messName = messSnapshot.child("MessName").getValue(String::class.java)

                    if(messName!=null){
                        messlist.add(messName)
                    }
                }
                mAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}