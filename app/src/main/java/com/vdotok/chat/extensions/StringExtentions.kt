package com.vdotok.chat.extensions

import java.util.regex.Pattern


fun String.containsNonAlphaNumeric() : Boolean {
    val p = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-¥¢£ø]")
    return p.matcher(this).find()
}

fun String.containsNonAlphaNumericName() : Boolean {
    val p = Pattern.compile("[!@#$%&*()+=|<>?{}\\[\\]~-¥¢£ø]")
    return p.matcher(this).find()
}


