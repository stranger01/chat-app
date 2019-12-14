package com.gameclubco.gurusocietyct.Adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.gameclubco.R
import com.gameclubco.gurusocietyct.Model.Message
//import com.gameclubco.sg.R
import com.gameclubco.gurusocietyct.Services.UserDataService
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

// Custom adapter for the messages
class MessageAdapter(val context: Context, val messages: ArrayList<Message>): androidx.recyclerview.widget.RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindMessage(context, messages[position])
    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.message_list_view, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View?) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView!!){
        val userImage = itemView?.findViewById<ImageView>(R.id.messageUserImage)
        val timeStamp = itemView?.findViewById<TextView>(R.id.messageTimeStamp)
        val userName = itemView?.findViewById<TextView>(R.id.messageUserName)
        val messageBody = itemView?.findViewById<TextView>(R.id.messageMessage)

        fun bindMessage (context: Context, message: Message){
            val resourceId = context.resources.getIdentifier(message.userAvatar,"drawable", context.packageName)
            userImage?.setImageResource(resourceId)
            userImage?.setBackgroundColor(UserDataService.returnAvatarColor(message.userAvatarColor))
            userName?.text = message.userName
            timeStamp?.text = returnDateString(message.timeStamp)
            messageBody?.text = message.message
        }

        // Parse the date to a better format
        fun returnDateString(isoString: String): String{

            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormatter.timeZone = TimeZone.getTimeZone("UTC")
            var convertedDate = Date()
            try {
                convertedDate =  isoFormatter.parse(isoString)
            }catch (e: ParseException){
                Log.d("PARSE", "Cannot parse date")
            }
            val outDateString = SimpleDateFormat("EEE, h:mm a", Locale.getDefault())
            return outDateString.format(convertedDate)

        }
    }
}