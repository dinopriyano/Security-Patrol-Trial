package com.dupat.demosecuritypatrol.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import com.dupat.demosecuritypatrol.network.repositories.LoginRepository
import com.dupat.demosecuritypatrol.utils.APIExceptions
import com.dupat.demosecuritypatrol.utils.Corountines
import com.dupat.demosecuritypatrol.utils.SingleLiveEvent
import com.dupat.demosecuritypatrol.viewmodel.state.ViewState

class LoginViewModel: ViewModel() {
    var username: String? = null
    var password: String? = null
    private var state : SingleLiveEvent<ViewState> = SingleLiveEvent()

    fun onLoginClick(v: View){
        state.value = ViewState.IsLoading(true)
        when {
            username.isNullOrEmpty() -> {
                state.value = ViewState.Error("Username must be filled!","username")
            }
            password.isNullOrEmpty() -> {
                state.value = ViewState.Error("Password must be filled!","password")
            }
            else -> {
                Corountines.main {
                    try {
                        val response = LoginRepository().loginUser(username!!,password!!)
                        response.let {
                            state.value = ViewState.IsSuccess(1)
                            state.value = ViewState.SuccessMessage(it)
                            return@main
                        }

                        state.value = ViewState.Error(response.message!!)
                    }
                    catch (e: APIExceptions)
                    {
                        state.value = ViewState.Error(e.message!!)
                    }
                }
            }
        }
    }

    fun getState() = state
}