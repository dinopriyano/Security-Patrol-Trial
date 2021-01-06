package com.dupat.demosecuritypatrol.network.repositories

import com.dupat.demosecuritypatrol.network.APIInterface
import com.dupat.demosecuritypatrol.network.SafeAPIRequest
import com.dupat.demosecuritypatrol.network.response.WebResponse
import com.dupat.demosecuritypatrol.network.response.data.LoginData

class LoginRepository() : SafeAPIRequest() {
    suspend fun loginUser(username:String,pass:String) : WebResponse<LoginData>{
        return apiRequest { APIInterface().userLogin(username,pass) }
    }
}