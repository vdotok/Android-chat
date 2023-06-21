package com.vdotok.chat.ui.account.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.vdotok.network.di.module.RetrofitModule
import com.vdotok.network.models.CheckUserModel
import com.vdotok.network.models.LoginUserModel
import com.vdotok.network.models.SignUpModel
import com.vdotok.network.network.Result
import com.vdotok.network.repository.AccountRepository

class AccountViewModel : ViewModel() {

    var email: ObservableField<String> = ObservableField<String>("")
    var password: ObservableField<String> = ObservableField<String>("")
    var fullName: ObservableField<String> = ObservableField<String>()


    fun loginUser(projectId: String) = liveData {
        val service = RetrofitModule.provideRetrofitService()
        val repo = AccountRepository(service)
        emit(Result.Loading)
        emit(
            repo.login(
                LoginUserModel(
                    email.get().toString(),
                    password.get().toString(),
                    projectId
                )
            )
        )
    }

    fun checkEmailExist(email: String) = liveData {
        val service = RetrofitModule.provideRetrofitService()
        val repo = AccountRepository(service)
        emit(Result.Loading)
        emit(repo.emailAlreadyExist(CheckUserModel(email)))
    }

    fun signUp(model: SignUpModel) = liveData {
        val service = RetrofitModule.provideRetrofitService()
        val repo = AccountRepository(service)
        emit(Result.Loading)
        emit(repo.signUp(model))
    }
}