package com.norgic.vdotokchat.ui.dashBoard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.norgic.vdotokchat.R
import com.norgic.vdotokchat.databinding.ItemAllUserContactListBinding
import com.norgic.vdotokchat.databinding.UserRowBinding
import com.norgic.vdotokchat.models.UserModel



class SelectUserContactAdapter(private val list: List<UserModel>, private val callbacks: OnChatItemClickCallbackListner):
    RecyclerView.Adapter<SelectUserContactViewHolder>(), Filterable {
    var selection = false
    var dataList: ArrayList<UserModel> = ArrayList()
    var filteredItems: ArrayList<UserModel> = ArrayList()
    init {
        dataList.addAll(list)
        filteredItems.addAll(list)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectUserContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SelectUserContactViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: SelectUserContactViewHolder, position: Int) {
        val listData = dataList[position]
        holder.binding?.groupModel = listData

        holder.binding?.root?.setOnClickListener {
            callbacks.onItemClick(position)
            selection = true
        }

    }

    override fun getItemCount(): Int = dataList.size

    fun updateData(userModelList: List<UserModel>) {
        dataList.clear()
        dataList.addAll(userModelList)
        filteredItems.clear()
        filteredItems.addAll(userModelList)
        notifyDataSetChanged()

    }

    fun getSelectedUsers(): List<UserModel> {
        val users: ArrayList<UserModel> = ArrayList()

        for(user in filteredItems){
            when {
                user.isSelected -> users.add(user)
            }
        }
        return users
    }


    override fun getFilter(): Filter? {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    dataList = filteredItems
                } else {
                    val filteredList: ArrayList<UserModel> = ArrayList()
                    for (row in filteredItems) {

                        if (row.fullName?.split(" ")?.first()?.toLowerCase()?.startsWith(charString.toLowerCase()) == true
                            || row.fullName?.split(" ")?.last()?.toLowerCase()?.startsWith(charString.toLowerCase()) == true) {
                            filteredList.add(row)
                        }
                    }
                    dataList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = dataList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                dataList = filterResults.values as ArrayList<UserModel>
                callbacks.searchResult(dataList.size)
                notifyDataSetChanged()
            }
        }
    }

}

class SelectUserContactViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_all_user_contact_list, parent, false)) {
    var binding: ItemAllUserContactListBinding? = null
    init {
        binding = DataBindingUtil.bind(itemView)
    }


}
interface OnChatItemClickCallbackListner {
    fun onItemClick(position: Int)
    fun searchResult(position: Int)
}

