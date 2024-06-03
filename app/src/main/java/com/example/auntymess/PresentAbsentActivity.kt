package com.example.auntymess

import PresentAbsentAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auntymess.Models.AttendanceItemModel
import com.example.auntymess.Models.BalanceItemModel
import com.example.auntymess.databinding.ActivityPresentAbsentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PresentAbsentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresentAbsentBinding
    private lateinit var auth: FirebaseAuth
    private var attlist= mutableListOf<AttendanceItemModel>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAdapter: PresentAbsentAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPresentAbsentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()
        databaseReference= FirebaseDatabase.getInstance().getReference()

        val action=intent.getStringExtra("action")
        val activity=intent.getStringExtra("activity")
        val messId=intent.getStringExtra("messId")
        Log.d("MessIdBalance", "$messId")

        val userid = auth.currentUser?.uid

        if (activity=="Main") {
            if (action == "present") {
                mAdapter = PresentAbsentAdapter(attlist, true)
            } else {
                binding.presentabsentTv.text="ABSENT DATES"
                mAdapter = PresentAbsentAdapter(attlist, false)
            }

            val recyclerview = binding.attDatesRecyclerview
            recyclerview.layoutManager = LinearLayoutManager(this)
            recyclerview.adapter = mAdapter

            if (userid != null && messId!=null) {
                val attRef = databaseReference.child("MessOwners").child(messId).child("attendance").child(userid)

                attRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        attlist.clear()
                        if (snapshot.exists()) {
                            val attitem = snapshot.getValue(AttendanceItemModel::class.java)

                            if (attitem != null) {
                                val attItem = AttendanceItemModel(
                                    attitem.presentDates,
                                    attitem.absentDates
                                )

                                attlist.add(attItem)
                            }
                        }
                        mAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
            }
        }
        else{
            if (action == "present") {
                mAdapter = PresentAbsentAdapter(attlist, true)
            } else {
                binding.presentabsentTv.text="ABSENT DATES"
                mAdapter = PresentAbsentAdapter(attlist, false)
            }

            val recyclerview = binding.attDatesRecyclerview
            recyclerview.layoutManager = LinearLayoutManager(this)
            recyclerview.adapter = mAdapter


            val balId=intent.getStringExtra("bal_id")
            Log.d("balanceid", "$balId")

            if(userid!=null && balId!=null && messId!=null){
                val attRef = databaseReference.child("MessOwners").child(messId).child("balance").child(userid).child(balId)

                attRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        attlist.clear()
                        if (snapshot.exists()) {
                            val attitem = snapshot.getValue(BalanceItemModel::class.java)

                            if (attitem != null) {
                                val attItem = AttendanceItemModel(
                                    attitem.presentDates,
                                    attitem.absentDates
                                )

                                attlist.add(attItem)
                            }
                        }
                        mAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
            }
        }


    }
}