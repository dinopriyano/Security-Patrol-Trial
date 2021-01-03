package com.dupat.demosecuritypatrol.utils

import android.app.Application
import android.content.Context

class MyApplication(): Application() {

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext
    }

    companion object{
        var ctx: Context? = null
        fun getAppContext(): Context{
            return ctx!!
        }
    }

}