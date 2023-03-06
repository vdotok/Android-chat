package com.vdotok.chat.ui.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.vdotok.chat.R
import com.vdotok.chat.databinding.LayoutFragmentLoginBinding
import com.vdotok.chat.extensions.checkValidation
import com.vdotok.chat.extensions.checkedPassword
import com.vdotok.chat.extensions.showSnackBar
import com.vdotok.chat.extensions.toggleVisibility
import com.vdotok.chat.prefs.Prefs
import com.vdotok.chat.ui.account.viewmodel.AccountViewModel
import com.vdotok.chat.ui.dashBoard.ui.DashboardActivity.Companion.createDashboardActivity
import com.vdotok.chat.utils.disable
import com.vdotok.chat.utils.enable
import com.vdotok.chat.utils.saveResponseToPrefs
import com.vdotok.network.models.LoginResponse
import com.vdotok.network.network.HttpResponseCodes
import com.vdotok.network.network.NetworkConnectivity
import com.vdotok.network.network.Result


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


    private fun loginV2(){

        binding.signInBtn.disable()
        activity?.let { activity ->
            viewModel.loginUser().observe(activity) {
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