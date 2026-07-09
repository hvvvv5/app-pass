package com.passgo.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PassGoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}