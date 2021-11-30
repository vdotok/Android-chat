package com.vdotok.chat.ui.dashBoard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.vdotok.network.di.module.RetrofitModule
import com.vdotok.network.models.CreateGroupModel
import com.vdotok.network.network.Result
import com.vdotok.network.repository.GroupRepository
import com.vdotok.network.repository.UserListRepository


class AllUserListFragmentViewModel: ViewModel() {

    private val service = RetrofitModule.provideRetrofitService()
    private val groupRepo = GroupRepository(service)
    private val userListRepo = UserListRepository(service)

    fun createGroup(token: String, model: CreateGroupModel) = liveData {
        emit(Result.Loading)
        emit(groupRepo.createGroup(token, model))
    }

    fun getAllUsers(token: String) = liveData {
        emit(Result.Loading)
        emit(userListRepo.getAllUsers(token))
    }

}