package com.eric.wandroid.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage {
    FOLLOW_SYSTEM,
    CHINESE,
    ENGLISH
}

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

    fun appLanguage(): AppLanguage {
        return AppLanguage.entries.firstOrNull {
            it.name == preferences.getString(KEY_APP_LANGUAGE, AppLanguage.FOLLOW_SYSTEM.name)
        } ?: AppLanguage.FOLLOW_SYSTEM
    }

    fun setAppLanguage(language: AppLanguage) {
        preferences.edit()
            .putString(KEY_APP_LANGUAGE, language.name)
            .apply()
        applyAppLanguage(language)
    }

    fun applyAppLanguage() {
        applyAppLanguage(appLanguage())
    }

    private fun applyAppLanguage(language: AppLanguage) {
        val locales = when (language) {
            AppLanguage.FOLLOW_SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            AppLanguage.CHINESE -> LocaleListCompat.forLanguageTags("zh-CN")
            AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    companion object {
        private const val PREF_NAME = "wandroid_settings"
        private const val KEY_NIGHT_MODE_ENABLED = "night_mode_enabled"
        private const val KEY_APP_LANGUAGE = "app_language"
    }
}
