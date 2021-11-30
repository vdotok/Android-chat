package com.vdotok.chat.utils

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData

/**
 *
 * Monitors if network is available if any listener is active.
 * Uses Lifecycle components
 *
 * @author : Zohaib Amir
 */
class NetworkStatusLiveData(application: Application) : LiveData<Boolean?>() {
    private val connectivityManager: ConnectivityManager
    private val networkCallback: NetworkCallback
    override fun onActive() {
        super.onActive()
        val info = connectivityManager.activeNetworkInfo
        if (info != null) {
            postValue(info.isConnectedOrConnecting)
        } else {
            postValue(false)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val networkRequest = NetworkRequest.Builder().build()
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        }
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    init {
        connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                postValue(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                postValue(false)
            }
        }
    }
}