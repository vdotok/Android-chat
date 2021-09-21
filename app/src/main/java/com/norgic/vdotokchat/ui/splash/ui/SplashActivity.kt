package com.norgic.vdotokchat.ui.splash.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.norgic.vdotokchat.R
import com.norgic.vdotokchat.databinding.ActivitySplashBinding
import com.norgic.vdotokchat.extensions.showSnackBar
import com.norgic.vdotokchat.models.AuthenticationResponse
import com.norgic.vdotokchat.network.HttpResponseCodes
import com.norgic.vdotokchat.prefs.Prefs
import com.norgic.vdotokchat.ui.account.ui.AccountActivity.Companion.createAccountsActivity
import com.norgic.vdotokchat.ui.dashBoard.ui.DashboardActivity.Companion.createDashboardActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private lateinit var prefs: Prefs


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()

    }

    private fun init() {
        prefs = Prefs(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        performAuthOperations()
    }


    private fun handleSdkAuthResponse(response: AuthenticationResponse) {
        when(response.status) {
            HttpResponseCodes.SUCCESS.valueInInt -> {
//                prefs.sdkAuthResponse = response
                performAuthOperations()
            }
            else -> {
                binding.root.showSnackBar(response.message)
                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, 2000)
            }
        }
    }

    private fun performAuthOperations() {
        prefs.loginInfo?.let {
            startActivity(createDashboardActivity(this))
            finish()
        }?: kotlin.run {
            moveToAccountsActivity()
        }
    }

    private fun moveToAccountsActivity() {
        startActivity(createAccountsActivity(this))
        finish()
    }

    companion object {

        const val API_ERROR = "API_ERROR"

    }
}
