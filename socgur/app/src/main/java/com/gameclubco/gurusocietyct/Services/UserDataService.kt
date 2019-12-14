package com.gameclubco.gurusocietyct.Services

import android.graphics.Color
import com.gameclubco.gurusocietyct.Controller.App
import java.util.*

object UserDataService {

    // Default values at the beginning
    var id = ""
    var avatarColor = ""
    var avatarName = ""
    var email = ""
    var name = ""

    // Logout function to reset all the values
    fun logout(){
        id = ""
        avatarColor = ""
        avatarName = ""
        email = ""
        name = ""
        App.prefs.authToken = ""
        App.prefs.userEmail = ""
        App.prefs.isLoggedIn = false
        MessageService.clearChannels()
        MessageService.clearMessages()
    }

    // The API returns the AvatarColor in a String
    // example: [0.4554444444, 0.567777777, 0.7433333322, 1]
    // replace the "[" "]" and "," for an empty
    // and to get the RGB value, multiply the value for 255 and convert it to int
    fun returnAvatarColor(components: String): Int{

        val strippedColor = components
                .replace("[", "")
                .replace("]", "")
                .replace(",", "")

        var r = 0
        var g = 0
        var b = 0

        val scanner = Scanner(strippedColor)
        if (scanner.hasNext()){
            r = (scanner.nextDouble() * 255).toInt()
            g = (scanner.nextDouble() * 255).toInt()
            b = (scanner.nextDouble() * 255).toInt()
        }

        return Color.rgb(r,g,b)
    }

}