package com.norgic.vdotokchat.ui.dashBoard.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.norgic.chatsdks.models.Message
import com.norgic.chatsdks.models.Presence
import com.norgic.vdotokchat.R
import com.norgic.vdotokchat.databinding.ChatRowBinding
import com.norgic.vdotokchat.extensions.hide
import com.norgic.vdotokchat.extensions.invisible
import com.norgic.vdotokchat.extensions.show
import com.norgic.vdotokchat.extensions.showSnackBar
import com.norgic.vdotokchat.models.GroupModel
import com.norgic.vdotokchat.prefs.Prefs
import kotlin.collections.ArrayList


class AllGroupsListAdapter(private val context: Context, private val prefs: Prefs, private val username:String, list: List<GroupModel>, private val callback: InterfaceOnGroupMenuItemClick, private val getUnreadCount: (groupModel: GroupModel) -> Int, private val getLastMessge: (groupModel: GroupModel) -> ArrayList<Message>, private val groupItemClick: (groupModel: GroupModel) -> Unit):
    RecyclerView.Adapter<UserViewHolder>() {

    var items: ArrayList<GroupModel> = ArrayList()
    var filteredItems: ArrayList<GroupModel> = ArrayList()
    var clickedPostion: Int? = null

    var presenceList: ArrayList<Presence> = ArrayList()

    init {
        items.addAll(list)
        filteredItems.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return UserViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val model = items[position]
        clickedPostion = position
        getLastMessge.invoke(model)
        if (getLastMessge.invoke(model).size > 0) {

            holder.binding?.tvLastmessage?.show()
            if (getLastMessge.invoke(model).map { it.type }.last().toString().equals("text")){
                holder.binding?.tvLastmessage?.text = getLastMessge.invoke(model).map { it.content }.last().toString()
            } else {
                holder.binding?.tvLastmessage?.text = "Attachment"
            }
        } else {
            holder.binding?.tvLastmessage?.hide()
        }
        holder.binding?.groupModel = model
        if (getUnreadCount.invoke(model) > 0){
            holder.binding?.tvLastmessage?.hide()
            holder.binding?.imgCount?.show()
            holder.binding?.tvMessage?.show()
            holder.binding?.imgCount?.text = getUnreadCount.invoke(model).toString()
        } else {
            holder.binding?.imgCount?.hide()
            holder.binding?.tvMessage?.invisible()


        }

        if (model.autoCreated == 1){
            holder.binding?.tvStatus?.text = getOneToOneStatus(model)
            holder.binding?.status = holder.binding?.tvStatus?.text?.equals(context.resources.getString(R.string.offline)) == false
        }else{
            holder.binding?.tvStatus?.text = getOneToManyStatus(model)
            holder.binding?.status = holder.binding?.tvStatus?.text?.contains(context.resources.getString(R.string.offline)) == false
        }



        model.let {
           if(model.autoCreated == 1){
                it.participants.forEach {name->
                    if (name.fullname?.equals(username) == false) {
                        holder.binding?.groupTitle?.text = name.fullname

                    }
                }
            } else{
                holder.binding?.groupTitle?.text = it.groupTitle
            }
        }


        holder.itemView.setOnClickListener {
            groupItemClick.invoke(model)
        }

        holder.binding?.imgMore?.setOnClickListener {
            val popupWindowObj = showMenuPopupWindow(model)
            holder.binding?.imgMore?.x?.toInt()?.let { x ->
                holder.binding?.imgMore?.y?.toInt()?.let { y ->
                    popupWindowObj.showAsDropDown(holder.binding?.imgMore,
                            x + 50, y
                    )
                }
            }
        }
    }




    private fun showMenuPopupWindow(groupModel: GroupModel): PopupWindow {
        val popupWindow = PopupWindow()

        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.group_menu_items, null)

        val tvEdit: TextView = view.findViewById(R.id.btn_edit)

        if (groupModel.autoCreated == 0){
            tvEdit.isEnabled = true
            tvEdit.setTextColor(Color.BLACK)
        }else{
            tvEdit.isEnabled = false
            tvEdit.setTextColor(Color.GRAY)
        }

        tvEdit.setOnClickListener {
            callback.onEditClcik(groupModel)
            popupWindow.dismiss()
        }

        val tvDelete: TextView = view.findViewById(R.id.btn_delete)
        tvDelete.setOnClickListener {
            groupModel.id?.let {
                callback.onDeleteClcik(it)
                popupWindow.dismiss()
            }
        }

        popupWindow.isFocusable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view
        return popupWindow
    }

    private fun getOneToOneStatus(model: GroupModel): CharSequence {

        var status = context.resources.getString(R.string.offline)

        model.participants.forEach{ participantList ->

            presenceList.let { list ->
                list.forEach {
                    if (it.isOnline == 0 && it.username == participantList.refID) {
                        status = context.resources.getString(R.string.online)
                        return@forEach
                    }
                }
            }
        }

        return status
    }
    private fun getOneToManyStatus(model: GroupModel): CharSequence {

        val tempList = ArrayList<String>()
        val size = model.participants.size


        model.participants.forEach { participant ->
            presenceList.let { list ->
                list.forEach {
                    if (it.isOnline == 0 && it.username == participant.refID && tempList.contains(it.username).not()) {
                        tempList.add(it.username)
                    }
                }
            }
        }

        return if (tempList.size > 0){
            tempList.size.toString() + "/" + size +" "+ context.resources.getString(R.string.online)
        }else{
            tempList.size.toString() + "/" + size +" "+ context.resources.getString(R.string.offline)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(userModelList: List<GroupModel>) {
        items.clear()
        items.addAll(userModelList)
        filteredItems.clear()
        filteredItems.addAll(userModelList)
        notifyDataSetChanged()
    }

    fun updatePresenceData(list: ArrayList<Presence>) {
        presenceList.clear()
        presenceList.addAll(list)
    }

}
class UserViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.chat_row, parent, false)) {
    var binding: ChatRowBinding? = null
    init {
        binding = DataBindingUtil.bind(itemView)
    }


}
interface InterfaceOnGroupMenuItemClick {
    fun onEditClcik(groupModel: GroupModel)
    fun onDeleteClcik(position: Int)
}

