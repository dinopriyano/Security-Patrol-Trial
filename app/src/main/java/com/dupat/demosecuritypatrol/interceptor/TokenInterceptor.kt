package com.dupat.demosecuritypatrol.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(var token: String): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var req = chain.request()
        if(req.header("No-Auth") == null){
            if(!token.isNullOrEmpty()){
                req = req.newBuilder().addHeader("Authorization",token).build()
            }
        }

        return chain.proceed(req)
    }
}