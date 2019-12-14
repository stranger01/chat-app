package com.gameclubco.gurusocietyct.Services

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.gameclubco.gurusocietyct.Controller.App
import com.gameclubco.gurusocietyct.Utilities.*
import org.json.JSONException
import org.json.JSONObject

// Object(singleton) - same as in Java for the static classes
// to deal all the calls to the API that correspond to the authentication, login and creation of an user
object AuthService {

    // Function to register the user in the database through the API
    // using volley library for the call
    // sending the email and password to the API
    fun registerUser(email: String, password: String, complete : (Boolean) -> Unit){

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val registerRequest = object : StringRequest(Method.POST, URL_REGISTER, Response.Listener { response ->
            complete(true)
        },Response.ErrorListener { error ->
            Log.d("ERROR", "Could not register user $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        App.prefs.requestQueue.add(registerRequest)
    }

    // Make the login of the users already register in the database
    // receiving the token for authorization
    fun loginUser(email: String, password: String, complete: (Boolean) -> Unit){

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val loginRequest = object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener { response ->

            //This is where we parse the json object
            try{
                App.prefs.userEmail = response.getString("user")
                App.prefs.authToken = response.getString("token")
                App.prefs.isLoggedIn = true
                complete(true)
            }catch (e: JSONException){
                Log.d("JSON", "EXC:" + e.localizedMessage)
                complete(false)
            }


        }, Response.ErrorListener {error ->
            //Where we deal with our error
            Log.d("ERROR", "Could not login user $error")
            complete(false)
        }){
            //set our Type and BODY for the call
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        App.prefs.requestQueue.add(loginRequest)
    }

    // Creating the user with all fields filled
    // The API needs to first register an user (fun registerUser)
    // only then can create an user
    fun createUser(name : String, email: String, avatarName: String, avatarColor: String, complete: (Boolean) -> Unit){

        val jsonBody = JSONObject()
        jsonBody.put("name", name)
        jsonBody.put("email", email)
        jsonBody.put("avatarName", avatarName)
        jsonBody.put("avatarColor", avatarColor)
        val requestBody = jsonBody.toString()


        val createRequest = object  : JsonObjectRequest(Method.POST, URL_CREATE_USER, null, Response.Listener { response ->

            try {
                UserDataService.name = response.getString("name")
                UserDataService.email = response.getString("email")
                UserDataService.avatarName = response.getString("avatarName")
                UserDataService.avatarColor = response.getString("avatarColor")
                UserDataService.id = response.getString("_id")
                complete(true)

            }catch (e: JSONException){
                Log.d("JSON", "EXC:" + e.localizedMessage)
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not add user $error")
            complete(false)
        }){
            // Set our HEAD and BODY for the call
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }

        App.prefs.requestQueue.add(createRequest)
    }

    // Function to find the user, step after login, to get the rest of the user information
    // after receiving the token from the login, we send the email and token
    fun findUserbyEmail(context: Context, complete: (Boolean) -> Unit){

        val findUserRequest = object : JsonObjectRequest(Method.GET, "$URL_GET_USER${App.prefs.userEmail}", null, Response.Listener { response ->
            try {
                UserDataService.name = response.getString("name")
                UserDataService.email = response.getString("email")
                UserDataService.avatarName = response.getString("avatarName")
                UserDataService.avatarColor = response.getString("avatarColor")
                UserDataService.id = response.getString("_id")

                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
                complete(true)

            }catch (e: JSONException){
                Log.d("JSON", "EXC: " + e.localizedMessage)
            }

        }, Response.ErrorListener{ error ->
            Log.d("ERROR", "Could not find user: $error")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }

        }

        App.prefs.requestQueue.add(findUserRequest)
    }
}
