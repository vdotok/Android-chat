package com.vdotok.chat.ui.dashBoard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.vdotok.network.di.module.RetrofitModule
import com.vdotok.network.models.DeleteGroupModel
import com.vdotok.network.network.Result
import com.vdotok.network.repository.GroupRepository


class AllGroupsFragmentViewModel: ViewModel() {


    fun getAllGroups(token: String) = liveData {
        val service = RetrofitModule.provideRetrofitService()
        val groupRepo = GroupRepository(service)
        emit(Result.Loading)
        emit(groupRepo.getAllGroups(token))
    }

    fun deleteGroup(token: String, model: DeleteGroupModel) = liveData {
        val service = RetrofitModule.provideRetrofitService()
        val groupRepo = GroupRepository(service)
        emit(Result.Loading)
        emit(groupRepo.deleteGroup(token, model))
    }

}