package com.zen.boom.task

import android.app.Application
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant


class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            plant(DebugTree())
        }
    }
}