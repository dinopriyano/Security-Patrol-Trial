package com.dupat.demosecuritypatrol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnLogin -> {
                startActivity(Intent(this,HomeActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay)
            }
        }
    }
}