package com.norgic.vdotokchat.ui.dashBoard.ui

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.net.toUri
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.norgic.chatsdks.ChatManager
import com.norgic.chatsdks.models.*
import com.norgic.vdotokchat.R
import com.norgic.vdotokchat.databinding.LayoutChatFragmentBinding
import com.norgic.vdotokchat.dialogs.AttachmentGroupDialog
import com.norgic.vdotokchat.extensions.*
import com.norgic.vdotokchat.models.GroupModel
import com.norgic.vdotokchat.prefs.Prefs
import com.norgic.vdotokchat.ui.dashBoard.adapter.ChatListAdapter
import com.norgic.vdotokchat.ui.dashBoard.adapter.OnMediaItemClickCallbackListner
import com.norgic.vdotokchat.ui.fragments.ChatMangerListenerFragment
import com.norgic.vdotokchat.utils.*
import com.norgic.vdotokchat.utils.ConnectivityStatus.Companion.isConnected
import com.norgic.vdotokchat.utils.ImageUtils.calculateInSampleSize
import com.norgic.vdotokchat.utils.ImageUtils.copyFileToInternalStorage
import com.norgic.vdotokchat.utils.ImageUtils.encodeToBase64
import java.io.*
import java.nio.file.Files
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask


/**
 * Created By: Norgic
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
            binding.rcvMsgList.adapter = adapter
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
        if (msg.get().toString().isEmpty()) {
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
        binding.rcvMsgList.scrollToPosition(adapter.itemCount.minus(1))
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


    private fun SaveImage(
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

        val parcelFileDescriptor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity?.applicationContext?.contentResolver?.openFileDescriptor(imageurl!!, "w", null)
        } else {
            TODO("VERSION.SDK_INT < KITKAT")
        }

        val fileOutputStream = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)
        fileOutputStream.write(bytes)
        fileOutputStream.close()
        imageurl?.let { uri ->
            activity?.applicationContext?.let { context->
                file = File(copyFileToInternalStorage(context, uri, directoryName))
                contentValu.clear()
                activity?.applicationContext?.contentResolver?.update(uri, contentValu, null, null)

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



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var filePath: File? = null

        if (resultCode == Activity.RESULT_OK && requestCode == ApplicationConstants.REQUEST_CODE_GALLERY) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity?.applicationContext?.let { context->
                    val byteArray = ImageUtils.convertImageToByte(
                        context,
                        Uri.parse(data?.data.toString())
                    )

                    SaveImage(
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

            filePath = file
            fileType = 0
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_VIDEO) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity?.applicationContext?.let { context ->
                    data?.data?.let { data ->
                         VideoPath = ImageUtils.copyFileToInternalStorage(context, data, "video")
                        val VideoBytes = converFileToByteArray(VideoPath)
                        SaveImage(
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
            filePath = file
            fileType = 2
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUDIO) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity?.applicationContext?.let { context ->
                    data?.data?.let { data ->
                        val AudioPath = copyFileToInternalStorage(context, data, "audio")
                        val AudioBytes = converFileToByteArray(AudioPath)
                        SaveImage(
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
            filePath = file
            fileType = 1
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == CAMERA) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity?.applicationContext?.let { context->
                    val byteArray = ImageUtils.convertBitmapToByteArray(
                        data?.extras?.get("data") as Bitmap
                    )

                    SaveImage(
                        byteArray!!,
                        "${System.currentTimeMillis()}",
                        "image/jpeg",
                        "${Environment.DIRECTORY_PICTURES}/$directoryName",
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                }
            } else {


                file = getFileData(
                    activity as Context, getImageUri(
                        activity?.applicationContext!!, data?.extras?.get(
                            "data"
                        ) as Bitmap
                    ), MediaType.IMAGE
                )

            }

            filePath = file
            fileType = 0
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_DOC) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                data?.data?.let { uriData ->
                activity?.let { context ->
                    val filePath = copyFileToInternalStorage(context, uriData, "document")
                    val FileBytes = converFileToByteArray(filePath)
                      SaveImage(
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
            filePath  = file
            fileType = 3
        }
        filePath?.let {
            if(filePath.length() > ApplicationConstants.FILE_SIZE_LIMIT){
                binding.root.showSnackBar(R.string.file_size)
            } else {
                val byteArray = Files.readAllBytes(filePath.toPath())
                sendImageFileToGroup(filePath, byteArray, fileType)
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

    /**
     * Function to send Image File to a Chat
     * @param file file that needs to be sent to group
     * @param byteArray ByteArray of the converted file
     * */
    private fun sendImageFileToGroup(file: File, byteArray: ByteArray?, fileType: Int) {
        byteArray?.let { byteArray ->

            val chunks = divideArray(byteArray, ApplicationConstants.CHUNK_SIZE)

            chunks?.let { chunks ->
                val headerModel = HeaderModel(
                    prefs.loginInfo?.refId + System.currentTimeMillis(),
                    chunks.size,
                    byteArray.size,
                    file.extension,
                    groupModel?.channelName ?: "",
                    groupModel?.channelKey ?: "",
                    loginUserRefId,
                    fileType
                )
                (activity as DashboardActivity).publishCustomPacketMessage(
                    headerModel,
                    groupModel?.channelKey ?: "", groupModel?.channelName ?: ""
                )
                if (isConnected(activity as Context))
                    sendFilesChunksToGroup(headerModel, byteArray, fileType)

            }

        }

    }


    /**
     * Function to send chunks of a large file to a group
     * @param headerModel Header model of a file that we need to send to differentiate which file belongs to which header
     * @param byteArray ByteArray of the File
     * */
    private fun sendFilesChunksToGroup(
        headerModel: HeaderModel,
        byteArray: ByteArray,
        fileType: Int
    ) {

        val chunks = divideArray(byteArray, ApplicationConstants.CHUNK_SIZE)
        val messageId = prefs.loginInfo?.refId + System.currentTimeMillis()


        var filechunk: FileModel? = null
        chunks?.forEachIndexed { index, bytes ->

            filechunk = FileModel(
                messageId,
                headerModel.headerId,
                headerModel.topic,
                headerModel.key,
                headerModel.from,
                index + 1,
                encodeAndReplaceLineBreaks(bytes),
                fileType
            )
            filechunk?.let {
                listOfChunks.add(it)
            }
        }

        makeFileMap()

    }

    /**
     * Helper function to remove string values from a string
     * @param bytes ByteArray from which we need to remove objects
     * @return Returns a string in base64 after removing required objects
     * */
    /* there was an issue that when encoded  the base64 string had line breaks in it so it was not
    being parsed on JS side so wrote this method to replace the line breaks */
    private fun encodeAndReplaceLineBreaks(bytes: ByteArray): String {
        var encodedString = encodeToBase64(bytes)
        return encodedString.replace("\n", "")
    }

    /**
     * Helper Function to store file chunks
     * */
    private fun makeFileMap() {
        if (listOfChunks.isNotEmpty()) {
            if (!fileChunkMaps.containsKey(listOfChunks[0].headerId)){
                fileChunkMaps[listOfChunks[0].headerId] = listOfChunks
            }
            startSendingChunks()
        }
    }

    /**
     * Function to divide a byte array to server defined chunk size of a larger file
     * @param source ByteArray that we need to split into chunks
     * @param chunkSize Size at which the byteArray will be divided from
     * */
    private fun divideArray(source: ByteArray, chunksize: Int): Array<ByteArray>? {
        val ret = Array(Math.ceil(source.size / chunksize.toDouble()).toInt()) { ByteArray(chunksize) }
        var start = 0
        for (i in ret.indices) {
            ret[i] = Arrays.copyOfRange(source, start, start + chunksize)
            start += chunksize
        }
        return ret
    }

    /**
     * Function to initiate file chunk sharing
     * */
    private fun startSendingChunks() {
        val itr = fileChunkMaps.iterator()
        while (itr.hasNext()) {
            val model = itr.next().value.first()
            (activity as DashboardActivity).publishCustomPacketMessage(
                model,
                groupModel?.channelKey ?: "",
                groupModel?.channelName ?: ""
            )
            binding.progressBar.show()
        }
    }


    /**
     * Function to publish a message to the server with any type of classObject
     * @param classObject CassObject of type any which can take up any Parcelable class
     * @param key key from the User that is unique against every chat or group
     * @param toGroup unique group identifier such as unique channel_id etc
     * */
//    fun publishCustomPacketMessage(classObject: Any, key: String, toGroup: String) {
//        cManger.sendCustomMessage(classObject, key, toGroup)
//    }



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
        //TODO("Not yet implemented")
    }

    override fun onTopicSubscribe(topic: String) {
        //TODO("Not yet implemented")
    }

    override fun onNewMessage(message: Message) {
        if (message.key == groupModel?.channelKey) {
            binding.progressBar.hide()
            usersList.clear()
            adapter.addItem(message)
            sendAcknowledgeMsgToGroup(message)
            scrollToLast()
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

    override fun onReceiptReceived(model: ReadReceiptModel) {
        activity?.runOnUiThread {
            if((model.from != loginUserRefId))
                adapter.updateMessageForReceipt(model)
        }
    }

    override fun onBytesArrayReceived(payload: ByteArray?) {
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

    private fun openInboxFragment() {
        activity?.onBackPressed()
    }

    /** function to handle sending acknowledgment message to the group that the message is received and seen
     * @param myMessage MqttMessage object containing details sent for the acknowledgment in group
     * */
    private fun sendAcknowledgeMsgToGroup(myMessage: Message) {
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

}
