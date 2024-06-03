package com.example.auntymess.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auntymess.LoginActivity
import com.example.auntymess.databinding.MessItemBinding

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
            binding.profileName.text=stuitem

            binding.root.setOnClickListener {
                val context=binding.root.context
                val intent=Intent(context,LoginActivity::class.java)
                intent.putExtra("messName",stuitem)
                intent.putExtra("action","register")
                context.startActivity(intent)

            }
        }

    }
}