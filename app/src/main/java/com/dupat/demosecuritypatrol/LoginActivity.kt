package com.dupat.demosecuritypatrol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.dupat.demosecuritypatrol.databinding.ActivityLoginBinding
import com.dupat.demosecuritypatrol.network.response.WebResponse
import com.dupat.demosecuritypatrol.network.response.data.LoginData
import com.dupat.demosecuritypatrol.session.SharedPrefManager
import com.dupat.demosecuritypatrol.utils.snackbar
import com.dupat.demosecuritypatrol.utils.toast
import com.dupat.demosecuritypatrol.viewmodel.LoginViewModel
import com.dupat.demosecuritypatrol.viewmodel.state.ViewState
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var viewmodel: LoginViewModel
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)
        viewmodel = ViewModelProvider(this).get(LoginViewModel::class.java)
        binding.viewmodel = viewmodel

//        checkSession()
        handleUIState()
    }

    private fun checkSession(){
        if(SharedPrefManager.getString(this,"token") != ""){
            startActivity(Intent(this,HomeActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right,R.anim.stay)
            finish()
        }
    }

    private fun handleUIState(){
        viewmodel.getState().observer(this, androidx.lifecycle.Observer {
            when(it){
                is ViewState.IsLoading ->{
                    containerLogin.snackbar("Loading...")
                }
                is ViewState.Error-> {
                    containerLogin.snackbar(it.err!!)
                }
                is ViewState.SuccessMessage -> {
                    val data: WebResponse<LoginData> = it.value as WebResponse<LoginData>

                    SharedPrefManager.setString(this,"id",data.data!!.user.id)
                    SharedPrefManager.setString(this,"token",data.data!!.token)
                    SharedPrefManager.setString(this,"company_id",data.data!!.user.company_id.toString())
                    SharedPrefManager.setString(this,"username",data.data!!.user.username)
                    SharedPrefManager.setString(this,"name",data.data!!.user.name)
                    SharedPrefManager.setString(this,"email",data.data!!.user.email)

                    startActivity(Intent(this,SetDataSetActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right,R.anim.stay)
                    finish()
                }
            }
        })
    }

    override fun onClick(v: View?) {
    }
}