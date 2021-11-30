package com.vdotok.chat.extensions
import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.databinding.BindingAdapter
import com.google.android.material.snackbar.Snackbar
import com.vdotok.chat.R


fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.toggleVisibility() {
    if (this.visibility == View.VISIBLE) this.hide()
    else this.show()
}

fun View.showSnackBar(message: String?) {
    message?.let { Snackbar.make(this, it, Snackbar.LENGTH_LONG).show() }
}

fun View.showSnackBar(stringId: Int) {
    Snackbar.make(this, stringId, Snackbar.LENGTH_LONG).show()
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

@BindingAdapter("email")
fun View.checkedEmail(email: String): Boolean {
    return if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        this.showSnackBar(this.context.getString(R.string.invalid_email))
        false
    } else {
        true
    }
}

@BindingAdapter("name_email")
fun checkValidation(view:View ,input : String): Boolean {
    return if (input.contains("@") && input.contains(".com")) {
        view.checkedEmail(input)
    } else {
        view.checkedUserName(input)
    }
}

@BindingAdapter("username")
fun View.checkedUserName(username: String): Boolean {
    return if (username.containsNonAlphaNumericName() || username.length < 4 || username.length > 20
        || username.isEmpty() || TextUtils.isDigitsOnly(username)
    ) {
        this.showSnackBar(this.context.getString(R.string.invalid_username))
        false
    } else {
        true
    }
}

@BindingAdapter("password")
fun View.checkedPassword(password: String): Boolean {
    return if (password.containsNonAlphaNumeric() || password.length < 8 || TextUtils.isEmpty(password)) {
        this.showSnackBar(this.context.getString(R.string.invalid_password))
        false
    } else {
        true
    }
}

fun Activity.hideKeyboard() {

    val focusedView: View? = this.currentFocus
    if (focusedView is EditText) {
        (this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(focusedView.getWindowToken(), 0)
    }
}

