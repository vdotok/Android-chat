package com.vdotok.chat

import android.app.Application
import android.content.res.Configuration
import com.vdotok.chat.prefs.Prefs
import com.vdotok.connect.manager.ChatManager
import com.vdotok.chat.utils.ApplicationConstants
import com.vdotok.chat.utils.ApplicationConstants.PROJECT_ID
import com.vdotok.network.utils.Constants
import com.vdotok.network.utils.Constants.BASE_URL

class ChatDemoApp : Application() {
    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!

    private lateinit var prefs : Prefs
    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        // Required initialization logic here!
        setVariables()
    }

    private fun setVariables() {
//        if project id is set inside the files
        if (BASE_URL.isNotEmpty() && PROJECT_ID.isNotEmpty()) {
            prefs.userBaseUrl = BASE_URL
            prefs.userProjectId = PROJECT_ID
        } else { // value exists in prefs
            BASE_URL = prefs.userBaseUrl.toString()
            PROJECT_ID = prefs.userProjectId.toString()
        }
        ChatManager.getInstance(this).setConstants(PROJECT_ID)
    }
    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    override fun onLowMemory() {
        super.onLowMemory()
    }
}