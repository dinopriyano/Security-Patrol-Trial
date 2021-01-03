package com.dupat.demosecuritypatrol.network.response

import com.google.gson.annotations.SerializedName

class WebResponse<T> (
    @SerializedName("code") var code: Int? = null,
    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: T?  = null
)