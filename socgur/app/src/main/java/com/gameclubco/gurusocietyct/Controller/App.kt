package com.gameclubco.gurusocietyct.Controller

import android.app.Application
import com.gameclubco.gurusocietyct.Utilities.SharedPrefs

// Share the SharedPreferences across the all application
class App : Application() {

    companion object {
        lateinit var prefs: SharedPrefs
    }

    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)
        super.onCreate()
    }
}