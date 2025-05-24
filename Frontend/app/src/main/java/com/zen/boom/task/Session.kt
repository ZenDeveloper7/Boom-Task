package com.zen.boom.task

import android.util.Base64
import org.json.JSONObject

object Session {
    var token: String?
        set(value) {
            if (!value.isNullOrEmpty()) {
                SharedPreferenceHelper.putString(
                    "token",
                    value.toString()
                )
            }
        }
        get() = SharedPreferenceHelper.getString("token")


    fun getUserId(claim: String = "id"): String? {
        token?.let {
            val parts = it.split(".")
            if (parts.size != 3) return null
            val payload =
                String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
            val json = JSONObject(payload)
            return if (json.has(claim)) json.getString(claim) else null
        } ?: return null
    }
}