package com.dupat.demosecuritypatrol.network.repositories

import com.dupat.dupatlsp.network.APIInterface
import com.dupat.dupatlsp.network.SafeAPIRequest
import com.dupat.dupatlsp.network.response.WebResponse
import com.dupat.dupatlsp.network.response.data.LoginData

class LoginRepository() : SafeAPIRequest() {
    suspend fun loginUser(id:String,pass:String) : WebResponse<LoginData>{
        return apiRequest { APIInterface().userLogin(id,pass) }
    }
}