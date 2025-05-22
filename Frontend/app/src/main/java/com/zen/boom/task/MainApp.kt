package com.zen.boom.task

import android.app.Application
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant


class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()

        SharedPreferenceHelper.init(this)
        if (BuildConfig.DEBUG) {
            plant(DebugTree())
        }
    }
}