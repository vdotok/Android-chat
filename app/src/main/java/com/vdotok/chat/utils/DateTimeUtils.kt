package com.vdotok.chat.utils

import android.annotation.SuppressLint
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.vdotok.chat.R
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
fun timeCheck(milli: Long): String {
    val simpleDateFormat = SimpleDateFormat("hh:mm")
    return simpleDateFormat.format(milli)
}
fun showDeleteGroupAlert(
    activity: FragmentActivity?,
    dialogListener: DialogInterface.OnClickListener
) {
    activity?.let {
        val alertDialog = AlertDialog.Builder(it)
            .setMessage(activity.getString(R.string.delete_group))
            .setPositiveButton(R.string.delete, dialogListener)
            .setNegativeButton(R.string.cancel, null).create()
        alertDialog.show()
    }
}
