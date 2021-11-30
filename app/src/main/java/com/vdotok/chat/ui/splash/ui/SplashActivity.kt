package com.vdotok.chat.ui.splash.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.vdotok.chat.R
import com.vdotok.chat.databinding.ActivitySplashBinding
import com.vdotok.chat.prefs.Prefs
import com.vdotok.chat.ui.account.ui.AccountActivity.Companion.createAccountsActivity
import com.vdotok.chat.ui.dashBoard.ui.DashboardActivity.Companion.createDashboardActivity

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
}
