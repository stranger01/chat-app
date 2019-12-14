package com.gameclubco.gurusocietyct.Services

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.gameclubco.gurusocietyct.Controller.App
import com.gameclubco.gurusocietyct.Model.Channel
import com.gameclubco.gurusocietyct.Model.Message
import com.gameclubco.gurusocietyct.Utilities.URL_GET_CHANNELS
import com.gameclubco.gurusocietyct.Utilities.URL_GET_MESSAGES
import org.json.JSONException


// Object(singleton) - same as in Java for the static classes
// to deal all the calls to the API that correspond to the channels and messages
object MessageService {

    //channel array
    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

    // Function to get the channels from the API connected to the database
    // using volley library for the call
    // getting the channel name, description and id
    // and sending the header and body with authtoken
    fun getChannels(complete: (Boolean) -> Unit){

        val channelsRequest = object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null, Response.Listener { response ->
            clearChannels()
            try {

                for(x in 0 until response.length()){
                    val channel = response.getJSONObject(x)
                    val channelName = channel.getString("name")
                    val channelDesc = channel.getString("description")
                    val channelId = channel.getString("_id")

                    // Add the new channel to our channel array
                    val newChannel = Channel(channelName, channelDesc, channelId)
                    this.channels.add(newChannel)
                }
                complete(true)

            }catch (e: JSONException){
                Log.d("JSON", "EXC: " + e.localizedMessage)
                complete(false)
            }

        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not retrive channels")
            complete(false)

        } ){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }
        // Make the call using volley library
        App.prefs.requestQueue.add(channelsRequest)
    }

    // Function to get the messages like the function getChannels
    // same behavior as getChannels
    fun getMessages (channelId: String, complete: (Boolean) -> Unit){

        val url = "$URL_GET_MESSAGES$channelId"
        val messageRequest = object  : JsonArrayRequest(Method.GET, url, null, Response.Listener {response ->
            clearMessages()
            try {
                for( x in 0 until response.length()){
                    val message = response.getJSONObject(x)
                    val messageBody = message.getString("messageBody")
                    val channelId = message.getString("channelId")
                    val id = message.getString("_id")
                    val userName = message.getString("userName")
                    val userAvatar = message.getString("userAvatar")
                    val userAvatarColor = message.getString("userAvatarColor")
                    val timeStamp = message.getString("timeStamp")

                    val newMessage = Message(messageBody, userName,channelId, userAvatar, userAvatarColor, id, timeStamp)
                    this.messages.add(newMessage)
                }
                complete(true)

            }catch (e: JSONException){
                Log.d("JSON", "EXC: " + e.localizedMessage)
                complete(false)
            }
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not retrive channels")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }
        App.prefs.requestQueue.add(messageRequest)
    }

    fun clearMessages(){
        messages.clear()
    }

    fun clearChannels(){
        channels.clear()
    }
}