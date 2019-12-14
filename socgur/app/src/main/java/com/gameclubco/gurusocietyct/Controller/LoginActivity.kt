package com.gameclubco.gurusocietyct.Controller

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.gameclubco.R
//import com.gameclubco.sg.R
import com.gameclubco.gurusocietyct.Services.AuthService
import kotlinx.android.synthetic.main.activity_login.*

// Class that treats all the login procedure
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginSpinner.visibility = View.INVISIBLE
    }

    fun loginCreateUserClicked(view : View){
        val createUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserIntent)
        finish()
    }

    fun loginLoginUserClicked(view: View){

        enableSpinner(true)

        val email = loginEmailTxt.text.toString()
        val password = loginPassTxt.text.toString()
        hideKeyboard()

        // Verifies if the fields aren't empty, calls the loginUser function,
        // then findUserbyEmail to make the login
        if(email.isNotEmpty() && password.isNotEmpty()){
            AuthService.loginUser(email, password){ loginSuccess ->
                if(loginSuccess){
                    AuthService.findUserbyEmail(this){ findSuccess ->
                        if(findSuccess){
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
            Toast.makeText(this, "Please fill in both email and password", Toast.LENGTH_LONG).show()
        }

    }

    fun errorToast(){
        Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    // Enabling spinner and disable dependingon the responses
    fun enableSpinner(enable: Boolean){
        if(enable){
            loginSpinner.visibility = View.VISIBLE
        }else{
            loginSpinner.visibility = View.INVISIBLE
        }
        loginLoginBtn.isEnabled = !enable
        loginCreateUserBtn.isEnabled = !enable
    }

    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
