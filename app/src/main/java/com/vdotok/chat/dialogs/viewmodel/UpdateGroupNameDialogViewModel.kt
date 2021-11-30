package com.vdotok.chat.dialogs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.vdotok.network.di.module.RetrofitModule
import com.vdotok.network.models.UpdateGroupNameModel
import com.vdotok.network.repository.GroupRepository


class UpdateGroupNameDialogViewModel: ViewModel() {

    fun updateGroupName(token: String, model: UpdateGroupNameModel) = liveData {
        val service = RetrofitModule.provideRetrofitService()
        val repo = GroupRepository(service)
        emit(com.vdotok.network.network.Result.Loading)
        emit(repo.updateGroupName(token, model))
    }

}