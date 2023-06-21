package com.vdotok.chat.models

import com.vdotok.network.models.CreateGroupResponse

data class NotificationData(
    var data: Data
)

data class Data(
    var action: String, //Notification event enum
    var groupModel: CreateGroupResponse,
)
