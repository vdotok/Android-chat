package com.vdotok.chat.utils

import com.vdotok.chat.prefs.Prefs
import com.vdotok.connect.models.Connection
import com.vdotok.network.models.LoginResponse

fun saveResponseToPrefs(prefs: Prefs, response: LoginResponse?) {
    prefs.loginInfo = response

    response?.let {

        val connection = prefs.loginInfo?.messagingServer?.let { msgServerUrl ->
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