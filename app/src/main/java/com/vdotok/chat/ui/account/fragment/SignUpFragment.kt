package com.vdotok.chat.ui.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.vdotok.chat.R
import com.vdotok.chat.databinding.LayoutFragmentSignupBinding
import com.vdotok.chat.extensions.*
import com.vdotok.chat.prefs.Prefs
import com.vdotok.chat.ui.account.viewmodel.AccountViewModel
import com.vdotok.chat.ui.dashBoard.ui.DashboardActivity
import com.vdotok.chat.utils.*
import com.vdotok.chat.utils.ApplicationConstants.PROJECT_ID
import com.vdotok.network.models.LoginResponse
import com.vdotok.network.models.SignUpModel
import com.vdotok.network.network.HttpResponseCodes
import com.vdotok.network.network.NetworkConnectivity
import com.vdotok.network.network.Result


/**
 * Created By: Vdotok
 * Date & Time: On 5/3/21 At 1:26 PM in 2021
 */

class SignUpFragment: Fragment() {

    private lateinit var prefs: Prefs
    private lateinit var binding: LayoutFragmentSignupBinding
    private val viewModel: AccountViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = LayoutFragmentSignupBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel

        init()

        return binding.root
    }

    private fun init() {

        prefs = Prefs(activity)
        //  binding.customToolbar.title.text = getString(R.string.register_user)

        binding.btnSignUp.setOnClickListener {
            if (it.checkedUserName(viewModel.fullName.get().toString()) && it.checkedPassword(viewModel.password.get().toString()) && it.checkedEmail(viewModel.email.get().toString())) {
                checkUserEmail(viewModel.email.get().toString())
                binding.btnSignUp.disable()
            }
        }

        binding.tvSignIn.setOnClickListener {
            moveToLogin(it)
        }

        configureBackPress()
    }

    private fun checkUserEmail(email: String) {
        activity?.let { activity ->

            viewModel.checkEmailExist(email).observe(viewLifecycleOwner) {
                binding.btnSignUp.enable()
                when (it) {
                    Result.Loading -> {
                        binding.progressBar.toggleVisibility()
                    }
                    is Result.Success ->  {
                        binding.progressBar.toggleVisibility()
                        handleCheckFullNameResponse(it.data)
                    }
                    is Result.Failure -> {
                        binding.progressBar.toggleVisibility()
                        if (NetworkConnectivity.isNetworkAvailable(activity).not())
                            binding.root.showSnackBar(getString(R.string.no_network_available))
                        else
                            binding.root.showSnackBar(it.exception.message)
                    }
                }

            }
        }
    }

    private fun handleCheckFullNameResponse(response: LoginResponse) {
        when(response.status) {
            HttpResponseCodes.SUCCESS.value -> signUp()
            else -> binding.root.showSnackBar(response.message)
        }
    }


    private fun signUp() {
        binding.btnSignUp.disable()

        activity?.let { activity ->
            viewModel.signUp(
                SignUpModel(
                    viewModel.fullName.get().toString(),
                    viewModel.email.get().toString(),
                    viewModel.password.get().toString(),
                    project_id = PROJECT_ID
                )
            ).observe(viewLifecycleOwner) {
                binding.btnSignUp.enable()
                when (it) {
                    Result.Loading -> {
                        binding.progressBar.toggleVisibility()
                    }
                    is Result.Success -> {
                        binding.progressBar.toggleVisibility()
                        handleLoginResponse(it.data)
                    }
                    is Result.Failure -> {
                        binding.progressBar.toggleVisibility()
                        if (NetworkConnectivity.isNetworkAvailable(activity).not())
                            binding.root.showSnackBar(getString(R.string.no_network_available))
                        else
                            binding.root.showSnackBar(it.exception.message)
                    }
                }
            }
        }
    }

    private fun handleLoginResponse(response: LoginResponse) {
        when(response.status) {
            HttpResponseCodes.SUCCESS.value -> {
                saveResponseToPrefs(prefs, response)
                startActivity(activity?.applicationContext?.let { DashboardActivity.createDashboardActivity(it) })
            }
            else -> {
                binding.root.showSnackBar(response.message)
            }
        }
    }

    private fun moveToLogin(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_move_to_login_user)
    }

    private fun configureBackPress() {
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    moveToLogin(binding.root)
                }
            })
    }
}