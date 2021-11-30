package com.vdotok.network.repository

import com.vdotok.network.models.*
import com.vdotok.network.network.Result
import com.vdotok.network.network.api.ApiService
import com.vdotok.network.network.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject


/**
 * Created By: VdoTok
 * Date & Time: On 11/5/21 At 5:45 PM in 2021
 */
class GroupRepository  @Inject constructor(
    private val apiService: ApiService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun updateGroupName(token: String, model: UpdateGroupNameModel): Result<UpdateGroupNameResponseModel> {
        return safeApiCall(dispatcher) {
            apiService.updateGroupName(token, model)
        }
    }

    suspend fun createGroup(token: String, model: CreateGroupModel): Result<CreateGroupResponse> {
        return safeApiCall(dispatcher) {
            apiService.createGroup(token, model)
        }
    }

    suspend fun getAllGroups(token: String): Result<AllGroupsResponse> {
        return safeApiCall(dispatcher) {
            apiService.getAllGroups(token)
        }
    }

    suspend fun deleteGroup(token: String, model: DeleteGroupModel): Result<DeleteGroupResponseModel> {
        return safeApiCall(dispatcher) {
            apiService.deleteGroup(token, model)
        }
    }

}