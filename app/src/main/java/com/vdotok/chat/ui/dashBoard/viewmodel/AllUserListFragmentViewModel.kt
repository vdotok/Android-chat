package com.vdotok.chat.ui.dashBoard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.vdotok.network.di.module.RetrofitModule
import com.vdotok.network.models.CreateGroupModel
import com.vdotok.network.network.Result
import com.vdotok.network.repository.GroupRepository
import com.vdotok.network.repository.UserListRepository


class AllUserListFragmentViewModel: ViewModel() {


    fun createGroup(token: String, model: CreateGroupModel) = liveData {
        val service = RetrofitModule.provideRetrofitService()
        val groupRepo = GroupRepository(service)
        emit(Result.Loading)
        emit(groupRepo.createGroup(token, model))
    }

    fun getAllUsers(token: String) = liveData {
        val service = RetrofitModule.provideRetrofitService()
        val userListRepo = UserListRepository(service)
        emit(Result.Loading)
        emit(userListRepo.getAllUsers(token))
    }

}