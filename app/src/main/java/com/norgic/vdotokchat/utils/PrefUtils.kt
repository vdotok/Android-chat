package com.norgic.vdotokchat.utils

import com.norgic.chatsdks.models.Connection
import com.norgic.vdotokchat.models.LoginResponse
import com.norgic.vdotokchat.prefs.Prefs

fun saveResponseToPrefs(prefs: Prefs, response: LoginResponse?) {
    prefs.loginInfo = response

    response?.let {

        val connection = prefs.sdkAuthResponse?.messagingServer?.let { msgServerUrl ->
            response.refId?.let { refId ->
                response.authorizationToken?.let { token ->
                    Connection(
                            refId,
                            token,
                            msgServerUrl.host,
                            msgServerUrl.port,
                            true,
                            refId,
                            60,
                            true
                    )
                }
            }
        }
        prefs.mConnection = connection
    }

}
//tcp://ssl://vte3.vdotok.com:443:443