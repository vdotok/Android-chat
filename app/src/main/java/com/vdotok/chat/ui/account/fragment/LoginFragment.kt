package com.vdotok.chat.ui.account.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.vdotok.chat.R
import com.vdotok.chat.databinding.LayoutFragmentLoginBinding
import com.vdotok.chat.extensions.checkValidation
import com.vdotok.chat.extensions.checkedPassword
import com.vdotok.chat.extensions.showSnackBar
import com.vdotok.chat.extensions.toggleVisibility
import com.vdotok.chat.models.QRCodeModel
import com.vdotok.chat.prefs.Prefs
import com.vdotok.chat.ui.account.viewmodel.AccountViewModel
import com.vdotok.chat.ui.dashBoard.ui.DashboardActivity.Companion.createDashboardActivity
import com.vdotok.chat.utils.*
import com.vdotok.chat.utils.ApplicationConstants.PROJECT_ID
import com.vdotok.network.models.LoginResponse
import com.vdotok.network.network.HttpResponseCodes
import com.vdotok.network.network.NetworkConnectivity
import com.vdotok.network.network.Result
import com.vdotok.network.utils.Constants


/**
 * Created By: Vdotok
 * Date & Time: On 5/3/21 At 1:26 PM in 2021
 */
class LoginFragment: Fragment() {

    private lateinit var binding: LayoutFragmentLoginBinding
    private lateinit var prefs: Prefs

    private val viewModel: AccountViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = LayoutFragmentLoginBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel

        init()

        return binding.root
    }

    private fun init() {

        prefs = Prefs(activity)

        binding.scanner.performSingleClick{
            activity?.runOnUiThread {
                qrCodeScannerLauncher.launch(IntentIntegrator.forSupportFragment(this))
            }
        }

        binding.signInBtn.setOnClickListener {
            if (validateFields(it)) {
                loginV2()
            }
        }
        binding.signUpBtn.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_move_to_signup_user)
        }

    }

    private fun validateFields(view: View): Boolean {
        return view.checkedPassword(viewModel.password.get().toString()) && checkValidation(view, viewModel.email.get().toString())
    }

    private fun handleLoginResponse(response: LoginResponse) {
        when(response.status) {
            HttpResponseCodes.SUCCESS.value -> {
                saveResponseToPrefs(prefs, response)
                startActivity(activity?.applicationContext?.let { createDashboardActivity(it) })
            }
            else -> {
                binding.root.showSnackBar(response.message)
            }
        }
    }

    private val qrCodeScannerLauncher = registerForActivityResult(QrCodeScannerContract()){
        if (!it.contents.isNullOrEmpty()){
            Log.e("RESULT_INTENT", it.contents)
            val data: QRCodeModel? = Gson().fromJson(it.contents, QRCodeModel::class.java)
            prefs.userProjectId = data?.project_id.toString().trim()
            prefs.userBaseUrl = data?.tenant_api_url.toString().trim()
            if (!prefs.userProjectId.isNullOrEmpty() &&   !prefs.userBaseUrl.isNullOrEmpty()){
                PROJECT_ID = prefs.userProjectId.toString().trim()
                Constants.BASE_URL =  prefs.userBaseUrl.toString().trim()
            }
            Log.d("RESULT_INTENT",data.toString())
        }else{
            binding.root.showSnackBar("QR CODE is not correct!!!")
        }
    }


    private fun loginV2(){

        binding.signInBtn.disable()
        activity?.let { activity ->
            viewModel.loginUser(prefs.userProjectId.toString()).observe(activity) {
                binding.signInBtn.enable()
                when (it) {
                    Result.Loading -> {
                        binding.progressBar.toggleVisibility()
                    }
                    is Result.Success ->  {
                        binding.progressBar.toggleVisibility()
                        handleLoginResponse(it.data)
                    }
                    is Result.Failure -> {
                        binding.progressBar.toggleVisibility()
                        if(NetworkConnectivity.isNetworkAvailable(activity).not())
                            binding.root.showSnackBar(getString(R.string.no_internet))
                    }
                }
            }
        }
    }
}