package com.vdotok.chat.interfaces

import com.vdotok.connect.models.*

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
    fun receiveAttachment(msgId: String)
    fun attachmentProgress(msgId: String, progress: Int)
    fun attachmentReceivedFailed()
    fun attachmentProgressReceive(msgId: String, progress: Int){}
    fun onReceiptReceived(model: ReadReceiptModel)
    fun onBytesArrayReceived(payload: ByteArray?)
    fun onFileReceivedCompleted(headerModel: HeaderModel, byteArray: ByteArray, msgId: String)
    fun onChunkReceived(fileModel: FileModel)
    fun onConnectionFailed()
    fun onConnectionLost()
    fun onFileSendingComplete()
    fun onNotification(notification: String)
    fun attachmentSendingFailed(headerId:String){}
    fun downloadFileComplete(){}
}