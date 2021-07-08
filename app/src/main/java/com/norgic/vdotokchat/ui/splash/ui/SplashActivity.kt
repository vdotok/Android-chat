package com.norgic.vdotokchat.ui.splash.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.norgic.vdotokchat.R
import com.norgic.vdotokchat.databinding.ActivitySplashBinding
import com.norgic.vdotokchat.extensions.showSnackBar
import com.norgic.vdotokchat.extensions.toggleVisibility
import com.norgic.vdotokchat.models.AuthenticationRequest
import com.norgic.vdotokchat.models.AuthenticationResponse
import com.norgic.vdotokchat.network.HttpResponseCodes
import com.norgic.vdotokchat.network.RetrofitBuilder
import com.norgic.vdotokchat.prefs.Prefs
import com.norgic.vdotokchat.ui.account.ui.AccountActivity.Companion.createAccountsActivity
import com.norgic.vdotokchat.ui.dashBoard.ui.DashboardActivity.Companion.createDashboardActivity
import com.norgic.vdotokchat.utils.ApplicationConstants.HTTP_CODE_NO_NETWORK
import com.norgic.vdotokchat.utils.ApplicationConstants.SDK_API_KEY
import com.norgic.vdotokchat.utils.ApplicationConstants.PROJECT_ID
import com.norgic.vdotokchat.utils.safeApiCall
import com.norgic.vdotokchat.network.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

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

        initSdkAuth()
    }

    private fun initSdkAuth() {
        binding.progressBar.toggleVisibility()
        val service = RetrofitBuilder.makeSdkAuthRetrofitService(this)
        CoroutineScope(Dispatchers.IO).launch {
            val response = safeApiCall { service.authSDK (
                    AuthenticationRequest(
                            SDK_API_KEY,
                            PROJECT_ID)
            ) }

            withContext(Dispatchers.Main) {
                try {
                    when (response) {
                        is Result.Success -> {
                            handleSdkAuthResponse(response = response.data)
                        }
                        is Result.Error -> {
                            if (response.error.responseCode == HTTP_CODE_NO_NETWORK) {
                                binding.root.showSnackBar(getString(R.string.no_network_available))
                                Handler(Looper.getMainLooper()).postDelayed({
                                }, 2000)

                            } else {
                                binding.root.showSnackBar(response.error.message)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    finish()
                                }, 2000)
                            }
                        }
                    }
                } catch (e: HttpException) {
                    Log.e(API_ERROR, "splashActivty: ${e.printStackTrace()}")
                } catch (e: Throwable) {
                    Log.e(API_ERROR, "splashActivty: ${e.printStackTrace()}")
                }
                binding.progressBar.toggleVisibility()
            }
        }
    }

    private fun handleSdkAuthResponse(response: AuthenticationResponse) {
        when(response.status) {
            HttpResponseCodes.SUCCESS.valueInInt -> {
                prefs.sdkAuthResponse = response
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
