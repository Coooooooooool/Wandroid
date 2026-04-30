package com.eric.wandroid.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class AppSettingsStore(context: Context) {
    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isNightModeEnabled(): Boolean {
        return preferences.getBoolean(KEY_NIGHT_MODE_ENABLED, false)
    }

    fun setNightModeEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_NIGHT_MODE_ENABLED, enabled)
            .apply()
    }

    fun applyNightMode() {
        AppCompatDelegate.setDefaultNightMode(
            if (isNightModeEnabled()) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    companion object {
        private const val PREF_NAME = "wandroid_settings"
        private const val KEY_NIGHT_MODE_ENABLED = "night_mode_enabled"
    }
}
