package com.norgic.vdotokchat.interfaces

import com.norgic.chatsdks.models.*

/**
 * Interface that are to be implemented in order provide callbacks to fragments
 * */
interface FragmentRefreshListener {
    fun onConnectionSuccess()
    fun onTopicSubscribe(topic: String)
    fun onNewMessage(message: Message)
    fun onPresence(message: ArrayList<Presence>)
    fun onTypingMessage(message: Message)
    fun sendAttachment(msgId: String, fileType: Int)
    fun recieveAttachment(msgId: String)
    fun attachmentProgress(msgId: String, progress: Int)
    fun attachmentReceivedFailed()
    fun onReceiptReceived(model: ReadReceiptModel)
    fun onBytesArrayReceived(payload: ByteArray?)
    fun onFileReceivedCompleted(headerModel: HeaderModel, byteArray: ByteArray, msgId: String)
    fun onChunkReceived(fileModel: FileModel)
    fun onConnectionFailed()
    fun onConnectionLost()
}