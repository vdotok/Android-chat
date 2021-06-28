package com.norgic.vdotokchat.ui.fragments

import android.app.Activity
import android.util.Log
import androidx.fragment.app.Fragment
import com.norgic.vdotokchat.interfaces.FragmentRefreshListener
import com.norgic.vdotokchat.models.GroupModel
import com.norgic.vdotokchat.ui.dashBoard.ui.DashboardActivity


/**
 * Created By: Norgic
 * Date & Time: On 5/26/21 At 3:21 PM in 2021
 */
abstract class ChatMangerListenerFragment: Fragment(), FragmentRefreshListener {

//    var mListener: FragmentRefreshListener? = null

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
//        this.mListener = (activity as FragmentRefreshListener)
    }
}