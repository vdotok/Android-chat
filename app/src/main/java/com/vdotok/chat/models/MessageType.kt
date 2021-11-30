package com.vdotok.chat.models


/**
 * Created By: Vdotok
 * Date & Time: On 1/19/21 At 6:31 PM in 2021
 * */

/** MessageType enums used in Message Model class to inform which type of message we will be
 * sending eg: text, media, typing etc
 * */
enum class MessageType {
    text,
    MEDIA,
    FILE,
    THUMBNAIL,
    PATH,
    TYPING,
    ACKNOWLEDGE,
    typing,
    receipts
}

/** MediaType enums used in File Model class to inform which type of file we will be
 * sending eg: image, audio, video, file
 * */
enum class MediaType(value: Int) {

    IMAGE(0),
    AUDIO(1),
    VIDEO(2),
    FILE(3);

    var value: Int
        internal set

    init {
        this.value = value
    }
}