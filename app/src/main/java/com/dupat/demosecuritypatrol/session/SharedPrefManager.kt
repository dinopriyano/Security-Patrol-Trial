package com.dupat.demosecuritypatrol.session

import android.content.Context
import java.util.*

class SharedPrefManager {
    companion object{
        fun setString(ctx: Context, key: String, value: String)
        {
            val pref = ctx.getSharedPreferences("MyRef", Context.MODE_PRIVATE)
            pref.edit().apply {
                putString(key,value)
                apply()
            }
        }

        fun getString(ctx: Context, key: String) : String
        {
            val pref = ctx.getSharedPreferences("MyRef", Context.MODE_PRIVATE)
            return pref.getString(key,"")!!
        }

        fun clearSession(ctx: Context)
        {
            val pref = ctx.getSharedPreferences("MyRef", Context.MODE_PRIVATE)
            pref.edit().clear().apply()
        }
    }
}