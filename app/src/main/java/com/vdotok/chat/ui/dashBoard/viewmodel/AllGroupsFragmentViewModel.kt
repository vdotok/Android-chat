package com.vdotok.chat.ui.dashBoard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.vdotok.network.di.module.RetrofitModule
import com.vdotok.network.models.DeleteGroupModel
import com.vdotok.network.network.Result
import com.vdotok.network.repository.GroupRepository


class AllGroupsFragmentViewModel: ViewModel() {

    private val service = RetrofitModule.provideRetrofitService()
    private val groupRepo = GroupRepository(service)

    fun getAllGroups(token: String) = liveData {
        emit(Result.Loading)
        emit(groupRepo.getAllGroups(token))
    }

    fun deleteGroup(token: String, model: DeleteGroupModel) = liveData {
        emit(Result.Loading)
        emit(groupRepo.deleteGroup(token, model))
    }

}