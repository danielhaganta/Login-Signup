package com.android.storyapp.core.utils

import android.content.Context
import android.content.SharedPreferences

class Preferences(val context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(USER_PREF, 0)

    fun setValues(key: String, value: String) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun setValues(key: String, value: Boolean) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getStringValues(key: String): String? {
        return sharedPreferences.getString(key, "")
    }

    fun getBooleanValues(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    companion object {
        const val USER_PREF = "user_pref"

        const val USER_EMAIL = "user_email"
        const val USER_NAME = "user_name"
        const val USER_ID = "user_id"
        const val USER_TOKEN = "user_token"

        const val DARK_MODE_PREF = "dark_mode_pref"
    }
}