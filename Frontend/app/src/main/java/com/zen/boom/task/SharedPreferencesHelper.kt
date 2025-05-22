package com.zen.boom.task

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPreferenceHelper {

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs =
            context.applicationContext.getSharedPreferences("your_prefs_name", Context.MODE_PRIVATE)
    }

    fun putString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    fun getString(key: String): String? {
        return prefs.getString(key, null)
    }
}