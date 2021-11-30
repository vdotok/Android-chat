package com.vdotok.chat.ui.dashBoard.ui

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.vdotok.connect.models.*
import com.vdotok.chat.R
import com.vdotok.chat.databinding.LayoutChatFragmentBinding
import com.vdotok.chat.dialogs.AttachmentGroupDialog
import com.vdotok.chat.extensions.*
import com.vdotok.chat.prefs.Prefs
import com.vdotok.chat.ui.dashBoard.adapter.ChatListAdapter
import com.vdotok.chat.ui.dashBoard.adapter.OnMediaItemClickCallbackListner
import com.vdotok.chat.ui.fragments.ChatMangerListenerFragment
import com.vdotok.chat.utils.*
import com.vdotok.chat.utils.ApplicationConstants.REQUEST_CODE_GALLERY
import com.vdotok.chat.utils.ImageUtils.copyFileToInternalStorage
import com.vdotok.chat.utils.ImageUtils.encodeToBase64
import com.vdotok.connect.manager.ChatManager
import com.vdotok.connect.models.Message
import com.vdotok.network.models.GroupModel
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask


/**
 * Created By: Vdotok
 * Date & Time: On 5/3/21 At 1:26 PM in 2021
 */
class ChatFragment: ChatMangerListenerFragment(), OnMediaItemClickCallbackListner {

    private lateinit var binding: LayoutChatFragmentBinding
    lateinit var adapter: ChatListAdapter
    private lateinit var prefs: Prefs
    var groupModel : GroupModel? = null

    var file: File? = null
    var VideoPath: String? = null
    var fileType = 0
    val directoryName: String = "Vdotok-chat"
    var model: Message? = null


    private var cManger: ChatManager? = null
    var listOfChunks: ArrayList<FileModel> = ArrayList()
    var fileChunkMaps: MutableMap<String, ArrayList<FileModel>> = mutableMapOf()
    private val usersList = ArrayList<String>()


    private var timer: CountDownTimer? = null
    var title : ObservableField<String> = ObservableField<String>()
    var mMessageText : ObservableField<String> = ObservableField<String>()
    private var typingText : ObservableField<String> = ObservableField<String>()

    var showTypingText : ObservableBoolean = ObservableBoolean(false)

    private var loginUserRefId = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = LayoutChatFragmentBinding.inflate(inflater, container, false)
        prefs = Prefs(context)

        init()
        if (listOfChunks.isNotEmpty())
            listOfChunks.clear()


        return binding.root
    }

    private fun initUserListAdapter() {
        activity?.applicationContext?.let {
            adapter = ChatListAdapter(it, this, loginUserRefId, getSaveChat(), this, {}){
                sendAcknowledgeMsgToGroup(it)
            }
            adapter.setHasStableIds(true)
            binding.rcvMsgList.adapter = adapter
            scrollToLast()
        }
    }

    private fun getSaveChat(): List<Message> {
        return (activity as DashboardActivity).mapGroupMessages[groupModel?.channelName]
            ?: return ArrayList()
    }

    private fun init() {
        activity?.let {
            cManger = ChatManager.getInstance(it)
            prefs = Prefs(activity)
        }

        (activity as DashboardActivity).mListener = this

        groupModel = arguments?.get(GroupModel.TAG) as GroupModel?


        binding.chatToolbar.imgBack.setOnClickListener {
          activity?.hideKeyboard()
          openInboxFragment()
        }

        binding.chatToolbar.typingUserName = typingText


        setModelData()

        initUserListAdapter()
        setListeners()
    }

     fun getUserName(model: Message): String {
        val participants = groupModel?.participants?.find { it.refID == model.from}
         return participants?.fullname.toString()
    }


    private fun setModelData() {
        binding.chatToolbar.groupTitle = title
        binding.chatInputLayout.messageText = mMessageText
        mMessageText.set("")
        binding.chatInputLayout.imgMic.performSingleClick {
            selectAudio()
        }
        binding.chatToolbar.showTypingText = showTypingText

        groupModel?.let {
            if (it.participants.size <= 2){
                it.participants.forEach { userName->
                    if (!userName.fullname.equals(prefs.loginInfo?.fullName)) {
                        title.set(userName.fullname)
                    }
                }
            } else{
                title.set(it.groupTitle)
            }
        }
        loginUserRefId = prefs.loginInfo?.refId.toString()
    }


    private fun setListeners(){

        binding.chatInputLayout.edtWriteMessage.afterTextChanged {
            if (getMessageCheck(mMessageText)) {
                handleAfterTextChange(mMessageText.get().toString())
            }
        }

        binding.chatInputLayout.imgSend.setOnClickListener {
            if (getMessageCheck(mMessageText)) {
                sendTextMessage()
            }
        }

        binding.chatInputLayout.imgMore.performSingleClick {
            activity?.supportFragmentManager?.let { AttachmentGroupDialog(
                this::selectVideo,
                this::selectAudio,
                this::selectDoc,
                this::takePhotoFromCamera,
                this::openMap,
                this::openContact
            ).show(it, AttachmentGroupDialog.TAG) }
        }
        binding.chatInputLayout.imgGallery.performSingleClick {
                selectImage()
        }
    }

    private fun getMessageCheck(msg: ObservableField<String>): Boolean {
        if (msg.get().toString().trim().isEmpty()) {
            binding.chatInputLayout.imgSend.isEnabled = false
            binding.chatInputLayout.imgSend.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.grayish_3
                )
            )
            return false

        } else {
            binding.chatInputLayout.imgSend.isEnabled = true
            binding.chatInputLayout.imgSend.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.bold_green
                )
            )
           return true
        }
    }


    companion object {
        const val TYPING_START = "1"
        const val TYPING_STOP = "0"
        const val TAG_FRAGMENT_CHAT = "CHAT_FRAGMENT"
        const val REQUEST_CODE_VIDEO = 101
        const val REQUEST_CODE_DOC = 102
        const val REQUEST_CODE_AUDIO = 103
        const val CAMERA = 109
        @JvmStatic
        fun newInstance() = ChatFragment().apply {}
    }


    /**
     * Function to handle on text changes when user is writing a message
     * @param text String to observe text change
     * */
    private fun handleAfterTextChange(text: String) {

        //setSendButtonState(text)

        if(text.isNotEmpty()){
            sendTypingMessage(loginUserRefId, true)

            timer?.cancel()
            timer = object : CountDownTimer(1500, 1000) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    sendTypingMessage(loginUserRefId, false)
                }
            }.start()
        }
    }


    /**
     * Function to scroll recyclerview to last index
     * */
    private fun scrollToLast() {
        if (adapter.itemCount > 0)
            binding.rcvMsgList.smoothScrollToPosition(adapter.itemCount.minus(1))
    }


    /**
     * Function to send message upon clicking send button
     * */
    private fun sendTextMessage() {
        val text = mMessageText.get().toString()
        groupModel?.let {
            if(text.isNotEmpty()){
                val chatModel = Message(
                    System.currentTimeMillis().toString(),
                    it.channelName,
                    it.channelKey,
                    loginUserRefId,
                    MessageType.text,
                    text.trim(),
                    0f,
                    getIsGroupMessage(),
                    ReceiptType.SENT.value
                )
                cManger?.publishMessage(chatModel)
                mMessageText.set("")
            }
        }
    }

    private fun getIsGroupMessage(): Boolean {
        return groupModel?.participants?.size!! > 1
    }


    var tempUriToDeleteAfterSendFileComplete: Uri? = null
    private fun saveFileToStorage(
        bytes: ByteArray,
        displayName: String,
        mimeType: String,
        path: String,
        contentUri: Uri
    ) {

        val resol = activity?.applicationContext?.contentResolver
        val contentValu = ContentValues()
        contentValu.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        contentValu.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        contentValu.put(MediaStore.MediaColumns.RELATIVE_PATH, path)

        val imageurl = resol?.insert(contentUri, contentValu)

        val parcelFileDescriptor =
            activity?.applicationContext?.contentResolver?.openFileDescriptor(imageurl!!, "w", null)

        val fileOutputStream = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)
        fileOutputStream.write(bytes)
        fileOutputStream.close()
        imageurl?.let { uri ->
            activity?.applicationContext?.let { context->
                file = File(copyFileToInternalStorage(context, uri, directoryName))
                contentValu.clear()
                activity?.applicationContext?.contentResolver?.update(uri, contentValu, null, null)
                tempUriToDeleteAfterSendFileComplete = uri
            }
        }
    }

    private fun openMap(){
        binding.root.showSnackBar(R.string.in_progress)

    }

    private fun openContact(){
        binding.root.showSnackBar(R.string.in_progress)

    }

    private fun selectImage() {
        val pickPhoto = Intent(
            Intent.ACTION_GET_CONTENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        pickPhoto.type = "image/*"
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pickPhoto.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        startActivityForResult(pickPhoto, ApplicationConstants.REQUEST_CODE_GALLERY)
    }

    private fun selectAudio() {
        val pickAudio = Intent(
            Intent.ACTION_GET_CONTENT,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        )

        pickAudio.type = "audio/*"
        pickAudio.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pickAudio.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        startActivityForResult(pickAudio, REQUEST_CODE_AUDIO)
    }

    private fun selectVideo() {
        val pickVideo = Intent(
            Intent.ACTION_GET_CONTENT,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )

        pickVideo.type = "video/*"
        pickVideo.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pickVideo.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        startActivityForResult(pickVideo, REQUEST_CODE_VIDEO)
    }

    private fun selectDoc() {
        val pickDoc = Intent(Intent.ACTION_GET_CONTENT)
        pickDoc.type = "*/*"
        val mimeTypes = arrayOf(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // .doc & .docx
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",  // .ppt & .pptx
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // .xls & .xlsx
            "text/plain",
            "application/pdf",
            "application/zip",
            "application/vnd.android.package-archive"
        )
        pickDoc.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        pickDoc.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(pickDoc, REQUEST_CODE_DOC)
    }


    var selectedFile: File? = null
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){

            when (requestCode) {
                REQUEST_CODE_GALLERY -> {
                    handleSelectionFromGallery(data)
                }
                REQUEST_CODE_VIDEO -> {
                    handleSelectionFromVideos(data)
                }
                REQUEST_CODE_AUDIO -> {
                    handleSelectionFromAudio(data)
                }
                CAMERA -> {
                    handleSelectionFromCamera(data)
                }
                REQUEST_CODE_DOC -> {
                    handleSelectionFromDocuments(data)
                }
            }

            groupModel?.let {
                cManger?.sendFileToGroup(it.channelKey, it.channelName, selectedFile, fileType)
            }

        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }
    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }


    private fun handleSelectionFromGallery(data: Intent?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.applicationContext?.let { context->
                val byteArray = ImageUtils.convertImageToByte(context, Uri.parse(data?.data.toString()))

                saveFileToStorage(
                    byteArray!!,
                    "${System.currentTimeMillis()}",
                    "image/jpeg",
                    "${Environment.DIRECTORY_PICTURES}/$directoryName",
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
            }
        } else {

            file = getFileData(activity as Context, data?.data, MediaType.IMAGE)

        }

        selectedFile = file
        fileType = 0

    }


    private fun handleSelectionFromVideos(data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.applicationContext?.let { context ->
                data?.data?.let { data ->
                    VideoPath = ImageUtils.copyFileToInternalStorage(context, data, "video")
                    val VideoBytes = converFileToByteArray(VideoPath)
                    saveFileToStorage(
                        VideoBytes!!,
                        "${System.currentTimeMillis()}",
                        "video/mp4",
                        "${Environment.DIRECTORY_MOVIES}/$directoryName",
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    )
                }

            }
        } else {
            file = getFileData(activity as Context, data?.data, MediaType.VIDEO)
        }
        selectedFile = file
        fileType = 2

    }


    private fun handleSelectionFromDocuments(data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            data?.data?.let { data ->
                activity?.let { context ->
                    val filePath = copyFileToInternalStorage(context, data, "document")
                    val FileBytes = converFileToByteArray(filePath)
                    saveFileToStorage(
                        FileBytes!!,
                        "${System.currentTimeMillis()}",
                        "application/pdf",
                        "${Environment.DIRECTORY_DOCUMENTS}/$directoryName",
                        MediaStore.Files.getContentUri("external")
                    )
                }
            }
        } else {
            file = getFileData(activity as Context, data?.data, MediaType.FILE)
        }
        selectedFile  = file
        fileType = 3

    }


    private fun handleSelectionFromCamera(data: Intent?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.applicationContext?.let { context->
                val byteArray = ImageUtils.convertBitmapToByteArray(
                    data?.extras?.get("data") as Bitmap
                )

                saveFileToStorage(
                    byteArray!!,
                    "${System.currentTimeMillis()}",
                    "image/jpeg",
                    "${Environment.DIRECTORY_PICTURES}/$directoryName",
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
            }
        } else {
            file = getFileData(activity as Context, getImageUri(activity?.applicationContext!!, data?.extras?.get("data") as Bitmap), MediaType.IMAGE)

        }

        selectedFile = file
        fileType = 0

    }

    private fun handleSelectionFromAudio(data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity?.applicationContext?.let { context ->
                data?.data?.let { data ->
                    val AudioPath = copyFileToInternalStorage(context, data, "audio")
                    val AudioBytes = converFileToByteArray(AudioPath)
                    saveFileToStorage(
                        AudioBytes!!,
                        "${System.currentTimeMillis()}",
                        "audio/x-wav",
                        "${Environment.DIRECTORY_MUSIC}/$directoryName",
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    )
                }
            }
        } else {
            file = getFileData(activity as Context, data?.data, MediaType.AUDIO)

        }
        selectedFile = file
        fileType = 1
    }




    /**
     * Function to send typing message to other user that the "other user is typing"
     * @param userName User name of the user who is typing
     * @param isTyping Boolean object to inform a user has started typing
     * */
    private fun sendTypingMessage(userName: String, isTyping: Boolean) {

        groupModel?.let {
            val content = if(isTyping) TYPING_START else TYPING_STOP
            val chatModel = Message(
                System.currentTimeMillis().toString(),
                it.channelName,
                it.channelKey,
                userName,
                MessageType.typing,
                content,
                0f,
                getIsGroupMessage()
            )

            cManger?.sendTypingMessage(chatModel, chatModel.key, chatModel.to)
        }
    }

    override fun onConnectionSuccess() {
        groupModel?.let {
            cManger?.subscribeTopic(it.channelKey, it.channelName)
        }
    }

    override fun onTopicSubscribe(topic: String) {
        //TODO("Not yet implemented")
    }

    override fun onNewMessage(message: Message) {
        activity?.runOnUiThread {
            if (message.key == groupModel?.channelKey) {
                (activity as DashboardActivity).mapUnreadCount.clear()
                binding.progressBar.hide()
                usersList.clear()
                adapter.addItem(message)
                sendAcknowledgeMsgToGroup(message)
                scrollToLast()
            }
            else {
                AllGroupsFragment.messageUpdateLiveData.postValue(message)
            }
        }
    }


    override fun onPresence(message: ArrayList<Presence>) {
        //TODO("Not yet implemented")
    }

    override fun onTypingMessage(message: Message) {
        if (message.key == groupModel?.channelKey && (message.from == loginUserRefId).not()) {
            showOnTypingMessage(message)
        }
        Log.d(TAG_FRAGMENT_CHAT, message.toString())
    }

    override fun sendAttachment(msgId: String, fileType: Int) {

        activity?.runOnUiThread {

            val activityInstance = (activity as DashboardActivity)
            groupModel?.let { groupModel ->
                adapter.isSend = true
                if (fileType == MediaType.IMAGE.value) {
                    adapter.addItem(activityInstance.makeImageItemModel(file,getDummyheader(fileType),groupModel,msgId)!!)
                    activityInstance.updateMessageMapData(activityInstance.makeImageItemModel(file,getDummyheader(fileType),groupModel,msgId)!!)
                } else {
                    adapter.addItem(activityInstance.makeFileModel(file,getDummyheader(fileType),groupModel,msgId)!!)
                    activityInstance.updateMessageMapData(activityInstance.makeFileModel(file,getDummyheader(fileType),groupModel,msgId)!!)
                }
                scrollToLast()
            }

        }

    }

    override fun recieveAttachment(msgId: String) {
//        Toast.makeText(activity, "this", Toast.LENGTH_SHORT).show()

    }

    private fun getDummyheader(fileType: Int) : HeaderModel{
        return HeaderModel("",0,0,"","","",loginUserRefId,fileType,0)
    }

    override fun attachmentProgress(msgId: String, progress: Int) {
//        adapter.progress = progress.toFloat()
        if (progress == 100) {
            activity?.runOnUiThread {
                val itemObject =  adapter.items.find { it.id == msgId }
                val index = adapter.items.indexOf(itemObject)
                adapter.isSend = false
//                adapter.updateData(progress,index)
                adapter.notifyItemChanged(index)
//
//
            }
        }


    }

    override fun attachmentReceivedFailed() {
        binding.root.showSnackBar(R.string.attachment_failed)

    }

    override fun onReceiptReceived(model: ReadReceiptModel) {
        activity?.runOnUiThread {
            if((model.from != loginUserRefId))
                adapter.updateMessageForReceipt(model)
        }
    }

    override fun onBytesArrayReceived(payload: ByteArray?) {
        activity?.runOnUiThread {

            payload?.let {
                val model = Message(
                    System.currentTimeMillis().toString(),
                    groupModel?.channelName ?: "",
                    groupModel?.channelKey ?: "",
                    loginUserRefId,
                    MessageType.media,
                    encodeToBase64(it),
                    0f,
                    getIsGroupMessage()
                )
                adapter.addItem(model)
            }
        }
    }

    override fun onFileReceivedCompleted(
        headerModel: HeaderModel,
        byteArray: ByteArray,
        msgId: String
    ) {
    }

    override fun onChunkReceived(fileModel: FileModel) {
        if (fileChunkMaps.containsKey(fileModel.headerId)) {
            var modelList = fileChunkMaps[fileModel.headerId]
            if (modelList?.size!! > 0) {
                modelList.remove(fileModel)
                fileChunkMaps[fileModel.headerId] = modelList
                if (modelList.isNotEmpty()) {
                    (activity as DashboardActivity).publishCustomPacketMessage(
                        modelList.first(),
                        groupModel?.channelKey ?: "",
                        groupModel?.channelName ?: ""
                    )
                }
            } else {
                if (fileChunkMaps.containsKey(fileModel.headerId)) {
                    fileChunkMaps.remove(fileModel.headerId)
                }
            }
        }
    }

    override fun onConnectionFailed() {
    }

    override fun onConnectionLost() {
    }

    override fun onFileSendingComplete() {
        activity?.runOnUiThread {
            deleteTempFile()
        }
    }

    private fun deleteTempFile() {
        val resolver = activity?.applicationContext?.contentResolver
        tempUriToDeleteAfterSendFileComplete?.let {
            val result: Int? = resolver?.delete(it, null, null)
            if (result != null && result > 0) {
                Log.d("Tag", "File deleted")
            }
        }
    }

    private fun openInboxFragment() {
        activity?.onBackPressed()
    }

    /** function to handle sending acknowledgment message to the group that the message is received and seen
     * @param myMessage MqttMessage object containing details sent for the acknowledgment in group
     * */
    private fun sendAcknowledgeMsgToGroup(myMessage: Message) {
        myMessage.status = ReceiptType.SEEN.value
        if(myMessage.from != loginUserRefId) {
            val receipt = ReadReceiptModel(
                ReceiptType.SEEN.value,
                myMessage.key,
                System.currentTimeMillis(),
                myMessage.id,
                loginUserRefId,
                myMessage.to
            )

            cManger?.publishPacketMessage(receipt, receipt.key, receipt.to)
        }
    }

    /**
     * Function to inflate typing message received view
     * @param message is the Typing Message Object that are receiving fom server
     * */
    private fun showOnTypingMessage(message: Message) {
        if(message.content == TYPING_START) {
            //binding.chatToolbar.status.show()
                message.from = getUserName(message)
                showTypingText.set(true)
                typingText.set(getNameOfUsers(message))
                hideTypingText()
            }

    }

    private fun openFolder() {
        val intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            intent.setDataAndType(
                Uri.parse(
                    Environment.getExternalStorageDirectory().toString()
                            + File.separator + "cPass" + File.separator
                ), "*/*"
            )

            activity?.startActivity(Intent.createChooser(intent, "Complete action using"))
        } else {

            intent.setDataAndType(
                Uri.parse(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString()
                            + File.separator + "cPass" + File.separator
                ), "*/*"
            )
            activity?.startActivity(Intent.createChooser(intent, "Open folder"))
        }

    }

    /**
     * Function to hide typing message received view
     * */
    private fun hideTypingText() {
        Timer().schedule(timerTask {
            activity?.runOnUiThread {
                //binding.chatToolbar.status.invisible()
                showTypingText.set(false)
            }
        }, 2000)
    }

    /**
     * Helper Function to get users of a typing message received
     * @param message is the Message Object of Users that are receiving fom server
     * */
    private fun getNameOfUsers(message: Message): String {
        if (usersList.contains(message.from).not()) {
            usersList.add(message.from)
        }

        return when (usersList.size) {
            0 -> ""
            1 -> "${usersList[0]} is typing..."
            2 -> "${usersList[usersList.size - 1]} and ${usersList[usersList.size - 2]} are typing..."
            else -> {
                val size = usersList.size
                "${usersList[size - 1]},${usersList[size - 2]} and ${size.minus(2)} others are typing..."
            }
        }
    }

    override fun onFileClick() {
        openFolder()
    }

    override fun onRecieptReceive(message: Message) {
        (activity as DashboardActivity).updateMessageMapData(message)
    }

}
