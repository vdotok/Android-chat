package com.vdotok.network.network.api

import com.vdotok.network.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("API/v0/Login")
    suspend fun loginUser(@Body model: LoginUserModel): LoginResponse

    @POST("API/v0/SignUp")
    suspend fun signUp(@Body model: SignUpModel): LoginResponse

    @POST("API/v0/CheckUsername")
    suspend fun checkUserName(@Body model: CheckUserModel): Response<LoginResponse>

    @POST("API/v0/CheckEmail")
    suspend fun checkEmail(@Body model: CheckUserModel): LoginResponse

    @POST("API/v0/AllUsers")
    suspend fun getAllUsers(@Header("Authorization") auth_token: String): GetAllUsersResponseModel

    @POST("API/v0/AllGroups")
    suspend fun getAllGroups(@Header("Authorization") auth_token: String): AllGroupsResponse

    @POST("API/v0/DeleteGroup")
    suspend fun deleteGroup(@Header("Authorization") auth_token: String, @Body model: DeleteGroupModel): DeleteGroupResponseModel

    @POST("API/v0/RenameGroup")
    suspend fun updateGroupName(@Header("Authorization") auth_token: String, @Body model: UpdateGroupNameModel): UpdateGroupNameResponseModel

    @POST("API/v0/CreateGroup")
    suspend fun createGroup(@Header("Authorization") auth_token: String, @Body model: CreateGroupModel): CreateGroupResponse

    @POST("API/v0/AuthenticateSDK")
    suspend fun authSDK(@Body model: AuthenticationRequest): Response<AuthenticationResponse>

    @Multipart
    @POST("/s3upload/")
    suspend fun uploadImage(@Part("type") type: RequestBody, @Part file: MultipartBody.Part?, @Part("auth_token") auth_token: RequestBody): UploadFileResponse
}