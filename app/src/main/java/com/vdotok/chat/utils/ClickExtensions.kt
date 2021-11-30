package com.vdotok.chat.utils

import android.view.View
import java.util.concurrent.TimeUnit

fun View.disableDoubleClick() {
    isClickable = false
    postDelayed({ isClickable = true }, TimeUnit.SECONDS.toMillis(2))
}

fun View.disableDoubleClickForHalfSec() {
    isClickable = false
    postDelayed({ isClickable = true }, TimeUnit.SECONDS.toMillis(500))
}

fun View.clicks(method:  () -> Unit) {
    setOnClickListener {
        method.invoke()
    }
}

fun View.performSingleClick(method: () -> Unit) {
    setOnClickListener {
        disableDoubleClick()
        method.invoke()
    }
}

fun View.performSingleClickShortDelay(method: () -> Unit) {
    setOnClickListener {
        disableDoubleClickForHalfSec()
        method.invoke()
    }
}

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}

/** function to connect to the socket server
 * @param timeInMillis set the delay time in millis
 * @param method method to call on click
 * */
fun View.disableClickForTime(timeInMillis: Long, method: () -> Unit) {
    setOnClickListener {
        method.invoke()
        isEnabled = false
        postDelayed({ isEnabled = true }, TimeUnit.SECONDS.toMillis(timeInMillis))
    }
}