package com.example.auntymess

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.auntymess.databinding.ActivitySpashScreenBinding

class SpashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySpashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.splashText.alpha=0f
        binding.splashText.animate().setDuration(2000).alpha(1f).withEndAction{
            startActivity(Intent(this,WelcomeActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            finish()
        }
        binding.animation.playAnimation()
    }
}