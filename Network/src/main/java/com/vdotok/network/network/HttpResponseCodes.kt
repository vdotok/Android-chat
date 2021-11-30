package com.vdotok.network.network


/**
 * Created By: Vdotok
 * Date & Time: On 5/7/21 At 12:08 PM in 2021
 *
 *
 * Enum Class to Handle response code in a much cleaner way
 */
enum class HttpResponseCodes(value: String) {

    SUCCESS("200"),
    ERROR("400");

    var value: String
        internal set

    var valueInInt: Int
        internal set

    init {
        this.value = value
        this.valueInInt = value.toInt()
    }
}