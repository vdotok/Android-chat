package com.vdotok.chat.ui.fragments

import android.app.Activity
import androidx.fragment.app.Fragment
import com.vdotok.chat.interfaces.FragmentRefreshListener


/**
 * Created By: Vdotok
 * Date & Time: On 5/26/21 At 3:21 PM in 2021
 */
abstract class ChatMangerListenerFragment: Fragment(), FragmentRefreshListener {

//    var mListener: FragmentRefreshListener? = null

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
//        this.mListener = (activity as FragmentRefreshListener)
    }
}