package com.gameclubco.gurusocietyct.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import com.gameclubco.R
import com.gameclubco.gurusocietyct.Adapters.MessageAdapter
import com.gameclubco.gurusocietyct.Model.Channel
import com.gameclubco.gurusocietyct.Model.Message
//import com.gameclubco.sg.R
import com.gameclubco.gurusocietyct.Services.AuthService
import com.gameclubco.gurusocietyct.Services.MessageService
import com.gameclubco.gurusocietyct.Utilities.BROADCAST_USER_DATA_CHANGE
import com.gameclubco.gurusocietyct.Services.UserDataService
import com.gameclubco.gurusocietyct.Utilities.SOCKET_URL
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter
    var selectedChannel: Channel? = null

    // Simple function to start the list adapters
    // channelAdapter is a simple list using an ArrayAdapter
    // messageAdapter has customs items, so it needs to have a diferent adapter, the MessageAdapter
    private fun setupAdapters(){
        channelAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter

        messageAdapter = MessageAdapter(this,MessageService.messages)
        messageListView.adapter = messageAdapter
        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        MobileAds.initialize(this,getString(R.string.admob_app_id))
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adView.visibility = View.GONE

        adView.adListener = object : AdListener(){
            override fun onAdLoaded(){
                adView.visibility = View.VISIBLE
                super.onAdLoaded()
            }
        }


        // Start the socket.IO sockets for the continuously communication
        // between the API and the Application in the messages and channel list
        socket.connect()
        socket.on("channelCreated", onNewChannel)
        socket.on("messageCreated", onNewMessage)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        setupAdapters()

        // Register a receiver for the broadcast, if there is some modifications in the login,
        // this receiver will receive the the data
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
                IntentFilter(BROADCAST_USER_DATA_CHANGE))

        // If one item of the channel list is clicked,
        // the recyclerView for the messages will have the messages of this channel
        channel_list.setOnItemClickListener { _, _, i, l ->
            selectedChannel = MessageService.channels[i]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }

        if(App.prefs.isLoggedIn){
            AuthService.findUserbyEmail(this){}
        }

    }

    // Unregister the broadcast and disconnect the socket
    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        socket.disconnect()
        super.onDestroy()
    }

    // The Broadcast receiver, receives info
    // verifies if the user is logged in so it can update the user information in the navigation drawer,
    // and also the channels and messages
    private val userDataChangeReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent?) {
            // When Broadcast is send out
            if( App.prefs.isLoggedIn){
                // The user is logged in
                userNameNavHeader.text = UserDataService.name
                userEmailNavHeader.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
                userImageNavHeader.setImageResource(resourceId)
                userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                loginBtnNavHeader.text = "Logout"

                MessageService.getChannels{complete ->
                    if(complete){
                        // Verify if there are channels and present the first one
                        if(MessageService.channels.count()>0){
                            selectedChannel = MessageService.channels[0]
                            channelAdapter.notifyDataSetChanged()
                            updateWithChannel()
                        }
                    }
                }
            }
        }
    }

    // Function to update the messageAdapter with the messages of the selected channel
    fun updateWithChannel(){
        mainChannelName.text = "#${selectedChannel?.name}"
        //download messages for channel
        if(selectedChannel != null){
            MessageService.getMessages(selectedChannel!!.id){ complete ->
                if(complete){
                    // Print out the messages
                    messageAdapter.notifyDataSetChanged()
                    if(messageAdapter.itemCount > 0){
                        messageListView.smoothScrollToPosition(messageAdapter.itemCount-1)
                    }
                }

            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // Function to create an alertDialog in order to add a new channel in the application,
    // and emiting it to the API in order to register the new channel
    fun addChannelClicked(view: View){

        if(App.prefs.isLoggedIn){
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                    .setPositiveButton("Add"){ _, _ ->

                        val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameTxt)
                        val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescTxt)
                        val channelName = nameTextField.text.toString()
                        val channelDesc = descTextField.text.toString()

                        // Create channel with the channel name and description
                        socket.emit("newChannel", channelName, channelDesc)
                    }
                    .setNegativeButton("Cancel"){ _, _ ->
                        // Cancel and close the dialog

                    }
                    .show()
        }
    }

    // Get the new channel created from the API
    private val onNewChannel = Emitter.Listener { args ->
        if(App.prefs.isLoggedIn){
            runOnUiThread {
                val channelName = args[0] as String
                val channelDesc = args[1] as String
                val channelID = args[2] as String

                val newChannel = Channel(channelName,channelDesc,channelID)
                MessageService.channels.add(newChannel)
                channelAdapter.notifyDataSetChanged()
            }
        }
    }

    // Get the new messages created
    private val onNewMessage = Emitter.Listener { args ->
        if(App.prefs.isLoggedIn){
            runOnUiThread{
                val channelId = args[2] as String
                if(channelId == selectedChannel?.id){
                    val msgBody = args[0] as String
                    val userName = args[3] as String
                    val userAvatar = args[4] as String
                    val userAvatarColor = args[5] as String
                    val id = args[6] as String
                    val timeStamp = args[7] as String

                    val newMessage = Message(msgBody,userName,channelId,userAvatar,userAvatarColor,id,timeStamp)
                    MessageService.messages.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    messageListView.smoothScrollToPosition(messageAdapter.itemCount-1)
                }
            }
        }
    }

    // If the user is already logged in, it will perform a logout,
    // in the other hand it will perform a login
    fun loginBtnNavClicked(view: View){

        if(App.prefs.isLoggedIn){

            // logout
            UserDataService.logout()
            channelAdapter.notifyDataSetChanged()
            messageAdapter.notifyDataSetChanged()
            userNameNavHeader.text = ""
            userEmailNavHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginBtnNavHeader.text = "Login"
            mainChannelName.text = "Please Log in"
        }else{
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    // Sending the written message to the API
    fun sendMessageBtnClicked(view: View){
        if(App.prefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null){
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id
            socket.emit("newMessage", messageTextField.text.toString(), userId, channelId,
                    UserDataService.name, UserDataService.avatarName, UserDataService.avatarColor)
            messageTextField.text.clear()
            hideKeyboard()
        }

    }

    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
