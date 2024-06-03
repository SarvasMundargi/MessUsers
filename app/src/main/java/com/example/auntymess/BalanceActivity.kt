package com.example.auntymess

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auntymess.Adapters.BalanceAdapter
import com.example.auntymess.Models.BalanceItemModel
import com.example.auntymess.databinding.ActivityBalanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BalanceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBalanceBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAdapter: BalanceAdapter
    private var ballist = mutableListOf<BalanceItemModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBalanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference()
        val messId = intent.getStringExtra("messId")
        Log.d("TAG", "$messId")

        mAdapter = BalanceAdapter(ballist, object : BalanceAdapter.OnItemClickListener {
            override fun OnPresentClick(balitem: BalanceItemModel) {
                navigateToPresentAbsentActivity("present", balitem, messId)
            }

            override fun OnAbsentClick(balitem: BalanceItemModel) {
                navigateToPresentAbsentActivity("absent", balitem, messId)
            }
        })

        val recyclerview = binding.recyclerviewBalance
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = mAdapter

        val userid = auth.currentUser?.uid

        if (userid != null && messId != null) {
            val balReference = databaseReference.child("MessOwners").child(messId).child("balance").child(userid)
            fetchBalanceData(balReference)
        }
    }

    private fun fetchBalanceData(balReference: DatabaseReference) {
        balReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ballist.clear()
                for (balsnapshot in snapshot.children) {
                    val balItem = balsnapshot.getValue(BalanceItemModel::class.java)
                    balItem?.let {
                        ballist.add(it)
                    }
                }
                mAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }

    private fun navigateToPresentAbsentActivity(action: String, balitem: BalanceItemModel, messId: String?) {
        val intent = Intent(this@BalanceActivity, PresentAbsentActivity::class.java).apply {
            putExtra("activity", "Balance")
            putExtra("action", action)
            putExtra("bal_id", balitem.balanceid)
            putExtra("messId", messId)
        }
        startActivity(intent)
        finish()
    }
}
