package com.zen.boom.task

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

}