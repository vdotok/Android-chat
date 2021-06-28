package com.norgic.vdotokchat.ui.dashBoard.ui

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.norgic.chatsdks.ChatManager
import com.norgic.chatsdks.ChatManagerCallback
import com.norgic.chatsdks.models.*
import com.norgic.chatsdks.models.Message
import com.norgic.vdotokchat.R
import com.norgic.vdotokchat.databinding.ActivityDashboardBinding
import com.norgic.vdotokchat.extensions.showSnackBar
import com.norgic.vdotokchat.interfaces.FragmentRefreshListener
import com.norgic.vdotokchat.models.GroupModel
import com.norgic.vdotokchat.prefs.Prefs
import com.norgic.vdotokchat.utils.ImageUtils
import com.norgic.vdotokchat.utils.ImageUtils.getBitmap
import com.norgic.vdotokchat.utils.NetworkStatusLiveData
import com.norgic.vdotokchat.utils.createAppDirectory
import com.norgic.vdotokchat.utils.saveFileDataOnExternalData
import java.io.File
import java.io.FileOutputStream

class DashboardActivity : AppCompatActivity(), ChatManagerCallback {

    private val MY_PERMISSIONS_REQUEST_CAMERA = 100
    private val MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 101
    private val MY_PERMISSIONS_REQUEST_READ_STORAGE = 102

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var prefs: Prefs
    val directoryName: String = "Vdotok-chat"

    private var chatManger: ChatManager? = null
    private var message: Message? = null
    var mListener: FragmentRefreshListener? = null
    private lateinit var myLiveData: NetworkStatusLiveData
    private var internetConnectionRestored = false
    var file: File? = null


    // To save messages locally per session
    var mapGroupMessages: MutableMap<String, ArrayList<Message>> = mutableMapOf()
    var mapUnreadCount: MutableMap<String, Int> = mutableMapOf()
    var mapLastMessage: MutableMap<String, ArrayList<Message>> = mutableMapOf()
    var savedPresenceList: ArrayList<Presence> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askForPermissions()
        init()
//        addInternetConnectionObserver()

    }

    private fun askForPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_CAMERA
            )
        }
    }


    private fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard)
        prefs = Prefs(this)
        prefs.clearPresenceData()

        //Initialize ChatManager for using ChatSDK methods
        initChatManager()

    }

    private fun initChatManager() {
        chatManger = ChatManager.getInstance(this)
        chatManger?.listener = this
        connect()

    }

    private fun connect() {
        prefs.mConnection?.let {
            it.interval = 5
            it.reConnectivity = false
            chatManger?.connect(it)
        }
    }
    fun publishCustomPacketMessage(classObject: Any, key: String, toGroup: String) {
        chatManger?.publishPacketMessage(classObject, key, toGroup)
    }

    /**
     * Function to send acknowledgement that file is fully received
     * @param message is the Message Object that we will be sending to the server
     * */
    fun sendAcknowledgmentOnMediaMessage(message: Message) {
        chatManger?.sendAcknowledgmentMessageReceived(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        chatManger?.disconnect()
    }

    companion object {

        fun createDashboardActivity(context: Context) = Intent(
                context,
                DashboardActivity::class.java
        ).apply {
            addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }
    }


    fun saveUpdatePresenceList(list: ArrayList<Presence>) {

        list.forEach {
            addUniqueElements(it)
        }
    }

    private fun addUniqueElements(mPresence: Presence) {

        var isUpdated = false

        savedPresenceList.forEachIndexed { index, presence ->
            if (presence.username == mPresence.username) {
                savedPresenceList[index] = mPresence
                isUpdated = true
            }
        }

        if (isUpdated.not()) {
            savedPresenceList.add(mPresence)
        }

    }

    fun getPresenceList(): ArrayList<Presence> {
        return savedPresenceList
    }


    /**
     * Function to help in persisting local chat by updating local data till the user is connected to the socket
     * @param message message object we will be sending to the server
     * */
    private fun updateMessageMapData(message: Message) {
        if (mapGroupMessages.containsKey(message.to)) {
            val messageValue: ArrayList<Message> = mapGroupMessages[message.to] as ArrayList<Message>
            messageValue.add(message)
            mapGroupMessages[message.to] = messageValue
            mapLastMessage[message.to] = messageValue

        } else {
            val messageValue: ArrayList<Message> = ArrayList()
            messageValue.add(message)
            mapGroupMessages[message.to] = messageValue
            mapLastMessage[message.to] = messageValue


        }
    }


    /**
     * Handle group presence after group is subscribed
     * @param group GroupModel consisting group related details
     * */
    fun handleSubscribedGroupData(group: GroupModel) {
        if (checkIfGroupIsSubscribed(group).not()) {
//            publishPresence(group)
            savedSubscribedGroupList.add(group)
        }
    }

    fun subscribe(group: GroupModel) {
        if (checkIfGroupIsSubscribed(group).not()) {
            doSubscribe(group)
//            publishPresence(group)
            savedSubscribedGroupList.add(group)
        }
    }

    private fun doSubscribe(model: GroupModel) {
        var dataSet = ArrayList<GroupModel>()
        dataSet.clear()
        dataSet.addAll(listOf(model))
        for (group in dataSet) {
            chatManger?.subscribeTopic(group.channelKey, group.channelName)
        }
    }

    /**
     * This function checks that whether the presence is published or not against local groups data storage
     * @param group GroupModel consisting group related details for subscription
     * */
    var savedSubscribedGroupList: ArrayList<GroupModel> = ArrayList()
    private fun checkIfGroupIsSubscribed(group: GroupModel): Boolean {
        savedSubscribedGroupList.let { item ->
            item.forEach {
                if (it.id == group.id) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Function to inform server that user is online hence providing online status to other users this user is connected to
     * @param groupModel is the group in which user will show online status
     * */
//     fun publishPresence(groupModel: GroupModel) {
//        chatManger?.publishPresence(groupModel.channelKey, groupModel.channelName)
//    }


    /**
     * Callback method when socket server is connected successfully
     * */
    //Callbacks
    override fun onConnect() {
        Log.e("Connection Status", "Connected")
            mListener?.onConnectionSuccess()
//        binding.root.showSnackBar(R.string.sdk_connected)
//        doSubscribe()
    }

    /**
     * Callback method when socket server failed to connect
     * @param cause is the error received from the server upon failed connection
     * */
    override fun onConnectionFailed(cause: Throwable) {
        chatManger = null
        mListener?.onConnectionFailed()
        Log.e("Connection Status", cause.toString())
    }

    override fun onConnectionLost(cause: Throwable) {
        chatManger = null
        mListener?.onConnectionLost()
        Log.e("Connection Status", cause.toString())
    }

    /**
     * Callback method when user is successfully subscribed to the server
     * @param topic is the value to differentiate groups and chats and perform connections later on
     * */
    override fun onSubscribe(topic: String) {
        Log.i("subscription Status", "successfully subscribed")
        mListener?.onTopicSubscribe(topic)

        prefs.getGroupList()?.let {
            for (group in it) {
                if (group.channelName == topic)
                    handleSubscribedGroupData(group)
            }
        }
    }

    /**
     * Callback method when user is not subscribed to the server
     * @param topic is the value to differentiate groups and chats and perform connections later on
     * @param cause is the error received from the server upon failed connection
     * */
    override fun onSubscribeFailed(topic: String, cause: Throwable?) {
        Log.e("subscription Failed", cause.toString())
    }

    /**
     * Callback method called when a presence of other user is received
     * @param who list of people who are showing presence i.e. online status
     * */
    override fun onPresenceReceived(who: ArrayList<Presence>) {
        runOnUiThread {
            saveUpdatePresenceList(who)
        }
        mListener?.onPresence(who)
    }

    /**
     * Callback method when other user has seen the message
     * @param model is the object received and will be used to view acknowledgement for a message
     * */
    override fun onReceiptReceived(model: ReadReceiptModel) {
        mListener?.onReceiptReceived(model)
    }

    /**
     * Callback method when user receives a message
     * @param myMessage message object we will be sending to the server
     * */
    override fun onMessageArrived(myMessage: Message) {
        mListener?.onNewMessage(myMessage)
        updateMessageMapData(myMessage)
    }

    /**
     * Callback method when user receives a typing message like "someone is typing"
     * @param myMessage message object we will be sending to the server
     * */
    override fun onTypingMessage(myMessage: Message) {
        mListener?.onTypingMessage(myMessage)
    }

    /**
     * Callback method when a ByteArray type message is received
     * @param payload is thee ByteArray formatted message object received directly
     * */
    override fun onBytesReceived(payload: ByteArray) {
        mListener?.onBytesArrayReceived(payload)
    }


    override fun onReceiveFileCompleted(headerModel: HeaderModel, byteArray: ByteArray, msgId: String) {
        checkAndroidVersionToSave(headerModel, byteArray)
        sendAttachmentMessage(headerModel, file, msgId)
    }

    fun sendAttachmentMessage(headerModel: HeaderModel, files: File?, msgId: String) {

        savedSubscribedGroupList.let { item ->
            item.forEach {
                if (it.channelName == headerModel.topic) {
                    getIsGroupMessage(it)
                    it.let {
                        when (headerModel.type) {

                            MediaType.IMAGE.value -> {
                                message = makeImageItemModel(files, headerModel, it, msgId)!!
                                message?.let { message ->
                                    updateMessageMapData(message)
                                    mListener?.onNewMessage(message)
                                }

                            }

                            MediaType.AUDIO.value -> {
                                message = makeFileModel(files, headerModel, it, msgId)!!
                                message?.let { message ->
                                    updateMessageMapData(message)
                                    mListener?.onNewMessage(message)
                                }

                            }
                            MediaType.VIDEO.value -> {
                                message = makeFileModel(files, headerModel, it, msgId)!!
                                message?.let { message ->
                                    updateMessageMapData(message)
                                    mListener?.onNewMessage(message)
                                }
                            }

                            MediaType.FILE.value -> {
                                message = makeFileModel(files, headerModel, it, msgId)!!
                                message?.let { message ->
                                    updateMessageMapData(message)
                                    mListener?.onNewMessage(message)
                                }
                            }

                            else -> {
                            }
                        }
                    }

                }
            }
        }

    }


    private fun checkAndroidVersionToSave(headerModel: HeaderModel, byteArray: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when (headerModel.type) {
                MediaType.IMAGE.value -> SaveImage(
                        byteArray,
                        "${System.currentTimeMillis()}",
                        "image/jpeg",
                        "${Environment.DIRECTORY_PICTURES}/$directoryName",
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                MediaType.VIDEO.value -> SaveImage(
                        byteArray,
                        "${System.currentTimeMillis()}",
                        "video/mp4",
                        "${Environment.DIRECTORY_MOVIES}/$directoryName",
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                )
                MediaType.AUDIO.value -> SaveImage(
                        byteArray,
                        "${System.currentTimeMillis()}",
                        "audio/x-wav",
                        "${Environment.DIRECTORY_MUSIC}/$directoryName",
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                )
                MediaType.FILE.value -> SaveImage(
                        byteArray,
                        "${System.currentTimeMillis()}",
                        "application/pdf",
                        "${Environment.DIRECTORY_DOCUMENTS}/$directoryName",
                        MediaStore.Files.getContentUri(
                                "external"
                        )
                )

            }

        } else {
            val fileName = "file_".plus(System.currentTimeMillis()).plus(".").plus(headerModel.fileExtension)
            val filePath = createAppDirectory(headerModel.type) + "/$fileName"
            file = saveFileDataOnExternalData(filePath, byteArray)

        }

    }

    //    private fun addInternetConnectionObserver() {
//        myLiveData = NetworkStatusLiveData(this.application)
//
//        myLiveData.observe(this, { isInternetConnected ->
//            when {
//                isInternetConnected == true && internetConnectionRestored -> {
////                    connect()
//                    mListener?.sub()
//                }
//                else -> {
//                    internetConnectionRestored = true
//                    Log.e("net","disconnect")
//                }
//            }
//        })
//    }

    private fun makeFileModel(
            file: File?,
            headerModel: HeaderModel,
            groupModel: GroupModel,
            msgId: String
    ): Message? {
        return file?.toUri()?.let {
            Message(
                    msgId,
                    groupModel.channelName,
                    groupModel.channelKey,
                    headerModel.from,
                    MessageType.media,
                    it.toString(),
                    0f,
                    getIsGroupMessage(groupModel),
                    ReceiptType.SENT.value,
                    headerModel.type
            )
        }
    }

    fun makeImageItemModel(
            file: File?,
            headerModel: HeaderModel,
            groupModel: GroupModel,
            msgId: String
    ): Message? {
        val bitmap = file?.let {
            com.norgic.vdotokchat.utils.getBitmap(it, 500, 500)
        }

        return bitmap?.let {
            ImageUtils.encodeToBase64(it)?.let { base64String ->
                Message(
                        msgId,
                        groupModel.channelName,
                        groupModel.channelKey,
                        headerModel.from,
                        MessageType.media,
                        base64String,
                        0f,
                        getIsGroupMessage(groupModel),
                        ReceiptType.SENT.value,
                        headerModel.type
                )
            }
        }
    }

    private fun getIsGroupMessage(group: GroupModel): Boolean {
        return group.participants.size > 1
    }

    private fun SaveImage(
            bytes: ByteArray,
            displayName: String,
            mimeType: String,
            path: String,
            contentUri: Uri
    ) {

        val resol = this?.applicationContext?.contentResolver
        val contentValu = ContentValues()
        contentValu.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        contentValu.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        contentValu.put(MediaStore.MediaColumns.RELATIVE_PATH, path)

        val imageurl = resol?.insert(contentUri, contentValu)

        val parcelFileDescriptor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this?.applicationContext?.contentResolver?.openFileDescriptor(imageurl!!, "w", null)
        } else {
            TODO("VERSION.SDK_INT < KITKAT")
        }

        val fileOutputStream = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)
        fileOutputStream.write(bytes)
        fileOutputStream.close()
        imageurl?.let { uri ->
            this?.applicationContext?.let { context ->
                file = File(ImageUtils.copyFileToInternalStorage(context, uri, directoryName))
                contentValu.clear()
                this?.applicationContext?.contentResolver?.update(uri, contentValu, null, null)

            }
        }
    }

    /**
     * Callback method when user a received a chunk of a large file
     * @param fileModel file chunk of a larger file that is being sent by the sender
     * */
    override fun onChunkReceived(fileModel: FileModel) {
        mListener?.onChunkReceived(fileModel)
    }

    /** callback fired if there is a fluctuation in network i.e. it is disconnected and reconnected
     * so to inform that the socket successfully reconnected again
     * @param connectionState Boolean informing the the socket is reconnected or not
     * */
    override fun reconnectAction(connectionState: Boolean) {
        //if (connectionState) doSubscribe()
    }

    /** callback fired to inform socket is connected or not before sending messages **/
    override fun connectionError() {
        Log.d("Connection Status", "connection error")
    }

}