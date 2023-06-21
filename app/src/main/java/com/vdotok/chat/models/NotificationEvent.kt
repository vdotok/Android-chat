package com.vdotok.chat.models

enum class NotificationEvent(value: String) {

    NEW("new"),
    MODIFY("modify"),
    DELETE("delete");

    var value: String
        internal set

    init {
        this.value = value
    }

}