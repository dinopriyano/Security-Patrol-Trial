package com.dupat.demosecuritypatrol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            startActivity(Intent(this,LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right,R.anim.stay)
            finish()
        },2000)
    }
}