package com.vdotok.chat.ui.dashBoard.ui

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.vdotok.chat.R
import com.vdotok.chat.databinding.LayoutFragmentInboxBinding
import com.vdotok.chat.dialogs.UpdateGroupNameDialog
import com.vdotok.chat.extensions.hide
import com.vdotok.chat.extensions.show
import com.vdotok.chat.extensions.showSnackBar
import com.vdotok.chat.extensions.toggleVisibility
import com.vdotok.chat.models.Data
import com.vdotok.chat.models.NotificationData
import com.vdotok.chat.models.NotificationEvent
import com.vdotok.chat.prefs.Prefs
import com.vdotok.chat.ui.account.ui.AccountActivity.Companion.createAccountsActivity
import com.vdotok.chat.ui.dashBoard.adapter.AllGroupsListAdapter
import com.vdotok.chat.ui.dashBoard.adapter.InterfaceOnGroupMenuItemClick
import com.vdotok.chat.ui.dashBoard.viewmodel.AllGroupsFragmentViewModel
import com.vdotok.chat.ui.fragments.ChatMangerListenerFragment
import com.vdotok.chat.utils.ApplicationConstants
import com.vdotok.chat.utils.ApplicationConstants.LOGIN_INFO
import com.vdotok.chat.utils.performSingleClick
import com.vdotok.chat.utils.showDeleteGroupAlert
import com.vdotok.connect.manager.ChatManager
import com.vdotok.connect.models.*
import com.vdotok.network.models.AllGroupsResponse
import com.vdotok.network.models.CreateGroupResponse
import com.vdotok.network.models.DeleteGroupModel
import com.vdotok.network.models.GroupModel
import com.vdotok.network.network.NetworkConnectivity
import com.vdotok.network.network.Result
import org.json.JSONArray


/**
 * Created By: Vdotok
 * Date & Time: On 5/11/21 At 2:15 AM in 2021
 */
class AllGroupsFragment : ChatMangerListenerFragment(), InterfaceOnGroupMenuItemClick {

    private lateinit var adapter: AllGroupsListAdapter

    val list = arrayListOf<String>()

    private var dataSet = ArrayList<GroupModel>()

    private val viewModel : AllGroupsFragmentViewModel by viewModels()

    private lateinit var binding: LayoutFragmentInboxBinding
    private lateinit var prefs: Prefs

    private lateinit var cManger: ChatManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = LayoutFragmentInboxBinding.inflate(inflater, container, false)
        prefs = Prefs(activity)


        init()

        return binding.root
    }

    private fun init() {

        prefs = Prefs(activity)
        activity?.let {
            cManger = ChatManager.getInstance(it)
        }

        (activity as DashboardActivity).mListener = this

        prefs.loginInfo?.let { info ->
            binding.customToolbar.title.text = info.fullName
        }

        messageUpdateLiveData.observe(this.viewLifecycleOwner) { message ->

            adapter.notifyDataSetChanged()

            sendAcknowledgeMsgToGroup(message)
        }

        binding.tvUsername.text = prefs.loginInfo?.fullName
        binding.isSocketConnected = (activity as DashboardActivity).isSocketConnected
        binding.tvLogout.setOnClickListener {
            cManger.disconnect()
            prefs.deleteKeyValuePair(LOGIN_INFO)
            activity?.let {
                startActivity(createAccountsActivity(it))
            }
        }
        binding.customToolbar.imgBack.hide()
        binding.customToolbar.title.text = getString(R.string.chat_room)
        binding.customToolbar.createGroupBtn.setImageResource(R.drawable.ic_plus1)


        binding.customToolbar.createGroupBtn.setOnClickListener {
            openContactFragment()
        }

        binding.btnNewChat.performSingleClick {
            openContactFragment()
        }

        binding.btnRefresh.performSingleClick {
            getAllGroups()
        }

        initRecyclerView()
        addPullToRefresh()
        getAllGroups()
    }


    private fun getAllGroups() {
        binding.swipeRefreshLay.isRefreshing = false
        binding.progressBar.toggleVisibility()

        activity?.let { activity ->
            viewModel.getAllGroups("Bearer ${prefs.loginInfo?.authToken}").observe(activity) {
                binding.progressBar.toggleVisibility()
                when (it) {
                    Result.Loading -> {
                        binding.progressBar.toggleVisibility()
                    }
                    is Result.Success ->  {
                        handleGroupSuccess(it.data)
                    }
                    is Result.Failure -> {
                        binding.progressBar.toggleVisibility()
                        if(NetworkConnectivity.isNetworkAvailable(activity).not())
                            binding.root.showSnackBar(getString(R.string.no_internet))
                    }
                }
            }
        }
    }

    private fun deleteGroup(model: DeleteGroupModel, groupModel: GroupModel) {
        binding.progressBar.toggleVisibility()

        activity?.let { activity ->
            viewModel.deleteGroup("Bearer ${prefs.loginInfo?.authToken}", model).observe(activity) {
                binding.progressBar.toggleVisibility()
                when (it) {
                    Result.Loading -> {
                        binding.progressBar.toggleVisibility()
                    }
                    is Result.Success ->  {
                        if (it.data.status == ApplicationConstants.SUCCESS_CODE) {
                            handleGroupDelete(it.data)
                            binding.root.showSnackBar(getString(R.string.group_deleted))
                        } else {
                            binding.root.showSnackBar(it.data.message)
                        }
                    }
                    is Result.Failure -> {
                        binding.progressBar.toggleVisibility()
                        if(NetworkConnectivity.isNetworkAvailable(activity).not())
                            binding.root.showSnackBar(getString(R.string.no_internet))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateGroupData()
    }

    private fun updateGroupData() {
        val groupData = (activity as DashboardActivity).groupListData
        adapter.updateData(groupData)
        if ((activity as DashboardActivity).groupListData.isEmpty()) {
            binding.groupChatListing.show()
        } else {
            binding.groupChatListing.hide()
        }
    }

    private fun addPullToRefresh() {
        binding.swipeRefreshLay.isEnabled = true
        binding.swipeRefreshLay.setOnRefreshListener {
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            getAllGroups()
        }
    }

    private fun handleGroupDelete(createGroupResponse: CreateGroupResponse) {
        (activity as DashboardActivity).groupListData.remove(createGroupResponse.groupModel)
        updateGroupData()
        createGroupResponse.let { model ->
            val dataModel = Data(
                action = NotificationEvent.DELETE.value,
                groupModel = createGroupResponse
            )
            val toList: JSONArray = JSONArray().apply {
                createGroupResponse.groupModel.participants.forEach {
                    it.refID?.let { it1 -> this.put(it1) }
                }
            }
            cManger.publishNotification(
                from = prefs.loginInfo?.refId.toString(),
                to = toList,
                data = Gson().toJson(dataModel)
            )
        }
    }

    private fun handleGroupSuccess(response: AllGroupsResponse) {
        if (response.groups?.isEmpty() == true) {
            binding.groupChatListing.show()
            binding.rcvUserList.hide()
            (activity as DashboardActivity).groupListData = ArrayList()
        } else {

            (activity as DashboardActivity).groupListData = response.groups as ArrayList<GroupModel>
            dataSet.clear()
            response.groups?.let { dataSet.addAll(it) }
            addLastMessageGroupToTop()

            binding.groupChatListing.hide()
            binding.rcvUserList.show()

            prefs.saveUpdateGroupList(dataSet)
            setGroupMapData(dataSet)

            doSubscribe()
        }
    }

    private fun addLastMessageGroupToTop() {

        if(prefs.getGroupList()?.size == dataSet.size){
            val lastGroupMessageKey = (activity as DashboardActivity).lastMessageGroupKey
            var lastUpdatedGroupIndex = -1
            var lastUpdatedGroupModel: GroupModel? = null

            dataSet.forEachIndexed { index, groupModel ->
                if(groupModel.channelKey == lastGroupMessageKey){
                    lastUpdatedGroupIndex = index
                    lastUpdatedGroupModel = groupModel
                    return@forEachIndexed
                }
            }
            lastUpdatedGroupModel?.let {
                dataSet.removeAt(lastUpdatedGroupIndex)
                dataSet.add(0, it)
            }
        }

        adapter.updateData(dataSet)
    }

    /**
     * Function to persist local chat till the user is connected to the socket
     * @param groupList list of all the groups user is connected to
     * */
    private fun setGroupMapData(groupList: ArrayList<GroupModel>) {
        if (groupList.isNotEmpty()) {
            groupList.forEach { groupModel ->
                if (!(activity as DashboardActivity).mapGroupMessages.containsKey(groupModel.channelName)) {
                    (activity as DashboardActivity).mapGroupMessages[groupModel.channelName] =
                        arrayListOf()
                }
            }
        }
    }


    private fun initRecyclerView() {
        activity?.applicationContext?.let {
            adapter = prefs.loginInfo?.fullName?.let { name ->
                AllGroupsListAdapter(
                    it,
                    prefs,
                    name,
                    ArrayList(),
                    this,
                    { groupModel: GroupModel -> getUnreadCount(groupModel) },
                    { groupModel: GroupModel -> getMessageList(groupModel) }) { groupModel: GroupModel ->
                    openChatFragment(
                        groupModel
                    )
                }
            }!!
            binding.rcvUserList.adapter = adapter
            adapter.updatePresenceData((activity as DashboardActivity).getPresenceList())
        }
    }


    /** function to handle sending acknowledgment message to the group that the message is received and seen
     * @param myMessage MqttMessage object containing details sent for the acknowledgment in group
     * */
    private fun sendAcknowledgeMsgToGroup(myMessage: Message) {

        prefs.loginInfo?.refId?.let {
            myMessage.status = ReceiptType.DELIVERED.value
            if (myMessage.from != it) {
                val receipt = ReadReceiptModel(
                    ReceiptType.DELIVERED.value,
                    myMessage.key,
                    System.currentTimeMillis(),
                    myMessage.id,
                    it,
                    myMessage.to
                )

                cManger.publishPacketMessage(receipt, receipt.key, receipt.to)
            }
        }
    }


    private fun openChatFragment(groupModel: GroupModel) {
        val bundle = Bundle()
        (activity as DashboardActivity).mapUnreadCount.remove(groupModel.channelName)
        bundle.putParcelable(GroupModel.TAG, groupModel)
        Navigation.findNavController(binding.root).navigate(R.id.action_open_chat_fragment, bundle)
    }

    private fun openContactFragment() {
        Navigation.findNavController(binding.root).navigate(R.id.action_open_contact_fragment)
    }

    override fun onConnectionSuccess() {
        doSubscribe()
    }

    override fun onTopicSubscribe(topic: String) {
//        for (group in dataSet) {
//            if (group.channelName == topic)
//                handleSubscribedGroupData(group)
//        }
    }

    override fun onNewMessage(message: Message) {
        activity?.runOnUiThread {
            addLastMessageGroupToTop()
            sendAcknowledgeMsgToGroup(message)
        }
    }


    private fun getUnreadCount(groupModel: GroupModel): Int {
        return (activity as DashboardActivity).mapUnreadCount[groupModel.channelName]
            ?: return 0
    }

    private fun getMessageList(groupModel: GroupModel): ArrayList<Message> {
        return (activity as DashboardActivity).mapLastMessage[groupModel.channelName]
            ?: return ArrayList()
    }

    override fun onPresence(message: ArrayList<Presence>) {
        saveAndUpdatePresenceList(message)
    }


    private fun saveAndUpdatePresenceList(presenceList: ArrayList<Presence>) {
        Log.d("presenceList", presenceList.toString())
        activity?.runOnUiThread {
            adapter.updatePresenceData((activity as DashboardActivity).getPresenceList())
            adapter.notifyItemRangeChanged(0, dataSet.size)
        }
    }

    override fun onTypingMessage(message: Message) {
        Log.d("", "")
    }

    override fun sendAttachment(msgId: String, fileType: Int) {
    }

    override fun receiveAttachment(msgId: String) {

    }

    override fun attachmentProgress(msgId: String, progress: Int) {
    }

    override fun attachmentReceivedFailed() {
    }

    override fun onReceiptReceived(model: ReadReceiptModel) {
        //TODO("Not yet implemented")
    }

    override fun onBytesArrayReceived(payload: ByteArray?) {
        //TODO("Not yet implemented")
    }

    override fun onFileReceivedCompleted(
        headerModel: HeaderModel,
        byteArray: ByteArray,
        msgId: String
    ) {
        activity?.runOnUiThread {
            if ((activity as DashboardActivity).mapUnreadCount.containsKey(headerModel.topic)) {
                val count = (activity as DashboardActivity).mapUnreadCount[headerModel.topic]
                (activity as DashboardActivity).mapUnreadCount[headerModel.topic] =
                    count?.plus(1) ?: 0
                adapter.notifyDataSetChanged()

            } else {
                (activity as DashboardActivity).mapUnreadCount[headerModel.topic] = 1
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onChunkReceived(fileModel: FileModel) {}

    override fun onConnectionFailed() {}

    override fun onConnectionLost() {}

    override fun onFileSendingComplete() {}


    private fun doSubscribe() {
        Handler(Looper.getMainLooper()).postDelayed({
            for (group in dataSet) {
                cManger.subscribeTopic(group.channelKey, group.channelName)
            }
        }, 2000)
    }

    private fun showDeleteGroupDialog(groupId: Int) {
        showDeleteGroupAlert(this.activity) { _, _ ->
            val model = DeleteGroupModel()
            model.groupId = groupId
            val deletedModel = dataSet.find { groupModel -> groupModel.id == groupId }
            deletedModel?.let { deleteGroup(model, it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onNotification(notification: String) {
        activity?.runOnUiThread {

            val dataModel = Gson().fromJson(notification, NotificationData::class.java)
            val data: ArrayList<GroupModel> = (activity as DashboardActivity).groupListData
            val groupData = dataModel.data.groupModel

            when(dataModel.data.action) {
                NotificationEvent.NEW.value -> {
                    binding.groupChatListing.hide()
                    if (!data.contains(groupData)){
                        data.add(groupData.groupModel)
                        adapter.updateData(data)
                        setGroupMapData((activity as DashboardActivity).groupListData)
                        dataSet.add(groupData.groupModel)
                        doSubscribe()
                    }
                }
                NotificationEvent.MODIFY.value -> {
                    data[data.indexOfFirst { groupModel -> groupModel.id == groupData.groupModel.id }] = groupData.groupModel
                    (activity as DashboardActivity).groupListData = data
                    adapter.updateData(data)
                }
                NotificationEvent.DELETE.value -> {
                    (activity as DashboardActivity).groupListData.removeIf { groupModel -> groupModel.id == groupData.groupModel.id}
                    updateGroupData()
                }
                else -> {}
            }
        }
    }


    override fun onEditGroupName(groupModel: GroupModel) {
        activity?.supportFragmentManager?.let {
            UpdateGroupNameDialog(
                groupModel,
                this::getAllGroups
            ).show(it, UpdateGroupNameDialog.UPDATE_GROUP_TAG)
        }

    }

    override fun onDeleteGroup(position: Int) {
        showDeleteGroupDialog(position)
    }

    override fun onDestroy() {
        super.onDestroy()
        cManger.disconnect()
    }

    companion object {
        val messageUpdateLiveData = MutableLiveData<Message>()
    }
}
