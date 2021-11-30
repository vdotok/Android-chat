package com.vdotok.chat.ui.account.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.vdotok.chat.utils.ApplicationConstants
import com.vdotok.network.di.module.RetrofitModule
import com.vdotok.network.models.CheckUserModel
import com.vdotok.network.models.LoginUserModel
import com.vdotok.network.models.SignUpModel
import com.vdotok.network.network.*
import com.vdotok.network.repository.AccountRepository

class AccountViewModel: ViewModel() {

    var email : ObservableField<String> = ObservableField<String>("")
    var password : ObservableField<String> = ObservableField<String>("")

    var fullName : ObservableField<String> = ObservableField<String>()


    private val service = RetrofitModule.provideRetrofitService()
    private val repo = AccountRepository(service)

    fun loginUser() = liveData {
            emit(Result.Loading)
            emit(repo.login(LoginUserModel(email.get().toString(), password.get().toString(), ApplicationConstants.PROJECT_ID)))
    }

    fun checkEmailExist(email: String) = liveData {
        emit(Result.Loading)
        emit(repo.emailAlreadyExist(CheckUserModel(email)))
    }

    fun signUp(model: SignUpModel) = liveData {
        emit(Result.Loading)
        emit(repo.signUp(model))
    }
}