package com.norgic.vdotokchat

import android.app.Application
import android.content.res.Configuration
import com.norgic.chatsdks.ChatManager

import com.norgic.vdotokchat.utils.ApplicationConstants

class ChatDemoApp : Application() {
    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    override fun onCreate() {
        super.onCreate()
        // Required initialization logic here!
        ChatManager.getInstance(this).setConstants(ApplicationConstants.PROJECT_ID)
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