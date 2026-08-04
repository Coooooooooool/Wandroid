package com.eric.wandroid

import android.app.Application
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.settings.AppSettingsStore

class WandroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppSettingsStore(this).apply {
            applyNightMode()
            applyAppLanguage()
        }
        NetworkModule.initialize(this)
    }
}
