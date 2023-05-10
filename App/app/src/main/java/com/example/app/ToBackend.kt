package com.example.app

import android.content.Context
import android.widget.Toast
import java.io.IOException
import okhttp3.*
import org.json.JSONObject

// Backend Server //////////////////////////////////////////////////////////////////////////////////
// used to relay x, y position back to our server
class BackendServer {

    // variables to hold the address of the backend and the frontend context in case of errors
    lateinit var applicationContext : Context
    var backendURL: String = ""
    var client    : OkHttpClient = TODO()

    // builds the BackendServer object
    fun constructor(mContext: Context) {
        backendURL = ""
        client = OkHttpClient()
        applicationContext = mContext
    }

    // converts current data (x, y and time) and converts to json string
    fun makeJSONStr(x: Double, y: Double): String {
        return JSONObject().apply {
            put("time", System.currentTimeMillis())
            put("x", x)
            put("y", y)
        }.toString()
    }

    fun reportLocation(x: Double, y: Double) {

        val request = Request.Builder().apply {
            url(backendURL)
            post(RequestBody.create(MediaType.parse("application/json"), makeJSONStr(x, y)))
        }.build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, exception: IOException) {
                Toast.makeText(applicationContext, exception.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Toast.makeText(applicationContext, response.toString(), Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}