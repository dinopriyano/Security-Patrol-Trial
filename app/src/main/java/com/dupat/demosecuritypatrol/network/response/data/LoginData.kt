package com.dupat.demosecuritypatrol.network.response.data

import com.dupat.demosecuritypatrol.model.UserModel
import com.google.gson.annotations.SerializedName

class LoginData (
    @SerializedName("user") var user: UserModel,
    @SerializedName("token") var token: String
)