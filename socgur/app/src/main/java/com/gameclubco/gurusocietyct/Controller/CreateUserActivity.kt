package com.gameclubco.gurusocietyct.Controller

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import com.gameclubco.R
//import com.gameclubco.sg.R
import com.gameclubco.gurusocietyct.Services.AuthService
import com.gameclubco.gurusocietyct.Utilities.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

// Class that treats all the register procedure
class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        createSpinner.visibility = View.INVISIBLE
    }

    // Using the Random to random pick an image and color(dark and light)
    fun generateUserAvatarClicked(view: View){
        val random = Random()
        val color = random.nextInt(2)
        val avatar = random.nextInt(28)

        if(color == 0){
            userAvatar = "light$avatar"
        }else{
            userAvatar = "dark$avatar"
        }
        val resourceID = resources.getIdentifier(userAvatar, "drawable", packageName)
        createAvatarImageView.setImageResource(resourceID)

    }

    // Generate a RGB color using the Random
    fun generateColorClicked(view: View){

        val random = Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        createAvatarImageView.setBackgroundColor(Color.rgb(r,g,b))

        val savedR = r.toDouble() / 255
        val savedG = g.toDouble() / 255
        val savedB = b.toDouble() / 255

        avatarColor = "[$savedR, $savedG, $savedB, 1]"

    }

    // Verifies is all fields are filled,
    // then calls registerUser, loginUser and createUser
    // After that it start to broadcast the modifications
    // for the other activities in order to be able to change the info
    fun createUserClicked (view: View){

        enableSpinner(true)

        val email = createEmailTxt.text.toString()
        val password = createPassTxt.text.toString()
        val userName = createUsernameTxt.text.toString()

        if(userName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()){
            AuthService.registerUser(email, password){registerSuccess ->
                if(registerSuccess){
                    AuthService.loginUser(email,password){loginSuccess->
                        if(loginSuccess){
                            AuthService.createUser(userName, email, userAvatar, avatarColor){createSuccess ->
                                if(createSuccess){
                                    //Create a broadcast to tell the modifications
                                    val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                    LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)
                                    enableSpinner(false)
                                    finish()
                                }else{
                                    errorToast()
                                }
                            }
                        }else{
                            errorToast()
                        }

                    }
                }else{
                    errorToast()
                }
            }
        }else{
            Toast.makeText(this, "Please make sure user name, email and password are filled in",Toast.LENGTH_LONG).show()
            enableSpinner(false)
        }
    }

    fun errorToast(){
        Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun enableSpinner(enable: Boolean){
        if(enable){
            createSpinner.visibility = View.VISIBLE
        }else{
            createSpinner.visibility = View.INVISIBLE
        }
        createCreateUserBtn.isEnabled = !enable
        createAvatarImageView.isEnabled = !enable
        createBackgroundColorBtn.isEnabled = !enable
    }
}
