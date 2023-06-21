package com.vdotok.network.network.api

import com.vdotok.network.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("Login")
    suspend fun loginUser(@Body model: LoginUserModel): LoginResponse

    @POST("SignUp")
    suspend fun signUp(@Body model: SignUpModel): LoginResponse

    @POST("CheckUsername")
    suspend fun checkUserName(@Body model: CheckUserModel): Response<LoginResponse>

    @POST("CheckEmail")
    suspend fun checkEmail(@Body model: CheckUserModel): LoginResponse

    @POST("AllUsers")
    suspend fun getAllUsers(@Header("Authorization") auth_token: String): GetAllUsersResponseModel

    @POST("AllGroups")
    suspend fun getAllGroups(@Header("Authorization") auth_token: String): AllGroupsResponse

    @POST("DeleteGroup")
    suspend fun deleteGroup(
        @Header("Authorization") auth_token: String,
        @Body model: DeleteGroupModel
    ): CreateGroupResponse

    @POST("RenameGroup")
    suspend fun updateGroupName(
        @Header("Authorization") auth_token: String,
        @Body model: UpdateGroupNameModel
    ): CreateGroupResponse

    @POST("CreateGroup")
    suspend fun createGroup(
        @Header("Authorization") auth_token: String,
        @Body model: CreateGroupModel
    ): CreateGroupResponse

    @POST("AuthenticateSDK")
    suspend fun authSDK(@Body model: AuthenticationRequest): Response<AuthenticationResponse>

    @Multipart
    @POST("/s3upload/")
    suspend fun uploadImage(
        @Part("type") type: RequestBody,
        @Part file: MultipartBody.Part?,
        @Part("auth_token") auth_token: RequestBody,
        @Part("extension") extension: RequestBody
    ): UploadFileResponse
}