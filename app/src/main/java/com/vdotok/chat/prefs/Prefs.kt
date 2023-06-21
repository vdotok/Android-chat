package com.vdotok.chat.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vdotok.chat.utils.ApplicationConstants.GROUP_MODEL_KEY
import com.vdotok.chat.utils.ApplicationConstants.LOGIN_INFO
import com.vdotok.chat.utils.ApplicationConstants.PRESENCE_MODEL_KEY
import com.vdotok.chat.utils.ApplicationConstants.SOCKET_CONNECTION
import com.vdotok.chat.utils.ApplicationConstants.USER_BASE
import com.vdotok.chat.utils.ApplicationConstants.USER_PROJECT
import com.vdotok.connect.models.Connection
import com.vdotok.network.models.GroupModel
import com.vdotok.network.models.LoginResponse
import java.lang.reflect.Type

/**
 * Created By: Vdotok
 * Date & Time: On 1/20/21 At 3:31 PM in 2021
 *
 * This class is mainly used to locally store and use data in the application
 * @param context the context of the application or the activity from where it is called
 */
class Prefs(context: Context?) {
    private val mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var userProjectId: String?
        get(){
            return mPrefs.getString(USER_PROJECT, "")
        }
        set(userInfo) {
            val mEditor: SharedPreferences.Editor = mPrefs.edit()
            mEditor.putString(USER_PROJECT, userInfo)
            mEditor.apply()
        }

    var userBaseUrl: String?
        get(){
            return mPrefs.getString(USER_BASE, "")
        }
        set(userInfo) {
            val mEditor: SharedPreferences.Editor = mPrefs.edit()
            mEditor.putString(USER_BASE, userInfo)
            mEditor.apply()
        }


    var mConnection: Connection?
        get(){
            val gson = Gson()
            val json = mPrefs.getString(SOCKET_CONNECTION, "")
            return gson.fromJson(json, Connection::class.java)
        }
        set(connection) {
            val mEditor: SharedPreferences.Editor = mPrefs.edit()
            val gson = Gson()
            val json = gson.toJson(connection)
            mEditor.putString(SOCKET_CONNECTION, json)
            mEditor.apply()
        }

    var loginInfo: LoginResponse?
        get(){
            val gson = Gson()
            val json = mPrefs.getString(LOGIN_INFO, "")
            return gson.fromJson(json, LoginResponse::class.java)
        }
        set(loginObject) {
            val mEditor: SharedPreferences.Editor = mPrefs.edit()
            val gson = Gson()
            val json = gson.toJson(loginObject)
            mEditor.putString(LOGIN_INFO, json)
            mEditor.apply()
        }

//    var sdkAuthResponse: AuthenticationResponse?
//        get(){
//            val gson = Gson()
//            val json = mPrefs.getString(SDK_AUTH_RESPONSE, "")
//            return gson.fromJson(json, AuthenticationResponse::class.java)
//        }
//        set(authResponse) {
//            val mEditor: SharedPreferences.Editor = mPrefs.edit()
//            val gson = Gson()
//            val json = gson.toJson(authResponse)
//            mEditor.putString(SDK_AUTH_RESPONSE, json)
//            mEditor.apply()
//        }

    /**
     * Function to save a list of any type in prefs
     * */
    private fun <T> setList(key: String, list: List<T>?) {
        val gson = Gson()
        val json = gson.toJson(list)
        set(key, json)
    }


    /**
     * Function to save a simple key value pair in prefs
     * */
    operator fun set(key: String?, value: String?) {
        val prefsEditor: SharedPreferences.Editor = mPrefs.edit()
        prefsEditor.putString(key, value)
        prefsEditor.apply()
    }

    /**
     * Function to get list of all groups saved in prefs
     * */
    fun getGroupList(): List<GroupModel>? {
        val gson = Gson()
        mPrefs.getString(GROUP_MODEL_KEY, null)?.let {
            val type: Type = object : TypeToken<List<GroupModel>>() {}.type
            return  gson.fromJson(it, type)
        }?: kotlin.run {
            return ArrayList()
        }
    }

    /**
     * Function to save updated list of groups in prefs
     * */
    fun saveUpdateGroupList(list: List<GroupModel>){
        setList(GROUP_MODEL_KEY, list)
    }

    /**
     * Function to clear all prefs from storage
     * */
    fun clearAll(){
        mPrefs.edit().clear().apply()
    }

    /**
     * Function to delete a specific prefs value from storage
     * */
    fun deleteKeyValuePair(key: String?) {
        mPrefs.edit().remove(key).apply()
    }

    fun clearPresenceData() {
        deleteKeyValuePair(PRESENCE_MODEL_KEY)
    }
}