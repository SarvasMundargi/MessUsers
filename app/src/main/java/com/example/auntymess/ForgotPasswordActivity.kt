package com.example.auntymess

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.auntymess.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.forgotActResetPasswordBtn.setOnClickListener {
            val email = binding.forgotActEmail.text.toString()


            if (email.isBlank()) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            } else {
                binding.progressBar.visibility= View.VISIBLE
                binding.forgotActResetPasswordBtn.visibility=View.GONE
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Reset Password link has been sent to your email",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.putExtra("action", "login")
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        binding.progressBar.visibility= View.GONE
                        binding.forgotActResetPasswordBtn.visibility=View.VISIBLE
                        Toast.makeText(
                            this,
                            "Failed to send reset email: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
