package com.vdotok.chat.models

import com.vdotok.connect.models.NotificationEvent
import com.vdotok.network.models.GroupModel

data class NotificationData(
    var data: Data
)

data class Data(
    var action: String, //Notification event enum
    var groupModel: GroupModel,
)
