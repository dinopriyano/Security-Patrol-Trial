package com.dupat.demosecuritypatrol.network

import android.util.Log
import com.dupat.demosecuritypatrol.utils.APIExceptions
import org.json.JSONObject
import retrofit2.Response


abstract class SafeAPIRequest {
    suspend fun <T : Any> apiRequest(call: suspend () -> Response<T>) : T{

        if(!internetIsConnected())
        {
            throw APIExceptions("No internet connection")
        }
        else
        {
            val response = call.invoke()

            if(response.isSuccessful)
            {
                return response.body()!!
            }
            else
            {
                val error = response.errorBody()?.string()
                val message = StringBuilder()

                error?.let {
                    try
                    {
                        message.append(JSONObject(it).getString("message"))
                    }
                    catch (e: Exception)
                    {
                        message.append("Error code ${response.code()}")
                    }
                }

                throw APIExceptions(message.toString())
            }
        }

    }

    open fun internetIsConnected(): Boolean {
        return try {
            val command = "ping -c 1 google.com"
            Runtime.getRuntime().exec(command).waitFor() == 0
        } catch (e: java.lang.Exception) {
            false
        }
    }
}