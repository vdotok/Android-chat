package com.vdotok.network.utils

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("visibleIf")
fun View.visibleIf(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.INVISIBLE
}