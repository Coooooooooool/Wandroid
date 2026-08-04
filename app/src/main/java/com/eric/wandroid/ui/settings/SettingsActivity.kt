package com.eric.wandroid.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.eric.wandroid.R
import com.eric.wandroid.common.storage.CacheUtils
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.eric.wandroid.common.ui.RemoteImageLoader
import com.eric.wandroid.data.settings.AppLanguage
import com.eric.wandroid.data.settings.AppSettingsStore
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var nightModeValueView: TextView
    private lateinit var toggleNightModeButton: Button
    private lateinit var languageValueView: TextView
    private lateinit var changeLanguageButton: Button
    private lateinit var clearCacheButton: Button
    private lateinit var cacheValueView: TextView
    private lateinit var versionValueView: TextView
    private lateinit var authorNameValueView: TextView
    private lateinit var authorEmailValueView: TextView

    private lateinit var settingsStore: AppSettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        settingsStore = AppSettingsStore(this)
        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, findViewById(R.id.settingsScrollView))
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        renderStaticContent()
        bindListeners()
        refreshCacheSize()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        nightModeValueView = findViewById(R.id.nightModeValue)
        toggleNightModeButton = findViewById(R.id.toggleNightModeButton)
        languageValueView = findViewById(R.id.languageValue)
        changeLanguageButton = findViewById(R.id.changeLanguageButton)
        clearCacheButton = findViewById(R.id.clearCacheButton)
        cacheValueView = findViewById(R.id.cacheValue)
        versionValueView = findViewById(R.id.versionValue)
        authorNameValueView = findViewById(R.id.authorNameValue)
        authorEmailValueView = findViewById(R.id.authorEmailValue)
    }

    private fun renderStaticContent() {
        renderNightModeState()
        renderLanguageState()
        versionValueView.text = buildVersionLabel()
        authorNameValueView.text = AUTHOR_NAME
        authorEmailValueView.text = AUTHOR_EMAIL
    }

    private fun bindListeners() {
        toggleNightModeButton.setOnClickListener {
            val nextEnabled = !settingsStore.isNightModeEnabled()
            settingsStore.setNightModeEnabled(nextEnabled)
            renderNightModeState()
            settingsStore.applyNightMode()
        }

        changeLanguageButton.setOnClickListener { showLanguageDialog() }

        clearCacheButton.setOnClickListener {
            clearCacheButton.isEnabled = false
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    CacheUtils.clearAppCache(this@SettingsActivity)
                    RemoteImageLoader.clearMemoryCache()
                }
                refreshCacheSize()
                clearCacheButton.isEnabled = true
                Snackbar.make(
                    findViewById(android.R.id.content),
                    R.string.settings_cache_cleared,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun renderNightModeState() {
        val enabled = settingsStore.isNightModeEnabled()
        nightModeValueView.text = if (enabled) {
            getString(R.string.settings_mode_night_enabled)
        } else {
            getString(R.string.settings_mode_day_enabled)
        }
        toggleNightModeButton.text = if (enabled) {
            getString(R.string.settings_switch_to_day)
        } else {
            getString(R.string.settings_switch_to_night)
        }
    }

    private fun renderLanguageState() {
        languageValueView.text = getString(
            when (settingsStore.appLanguage()) {
                AppLanguage.FOLLOW_SYSTEM -> R.string.settings_language_follow_system
                AppLanguage.CHINESE -> R.string.settings_language_chinese
                AppLanguage.ENGLISH -> R.string.settings_language_english
            }
        )
    }

    private fun showLanguageDialog() {
        val languages = AppLanguage.entries
        val labels = languages.map { language ->
            getString(
                when (language) {
                    AppLanguage.FOLLOW_SYSTEM -> R.string.settings_language_follow_system
                    AppLanguage.CHINESE -> R.string.settings_language_chinese
                    AppLanguage.ENGLISH -> R.string.settings_language_english
                }
            )
        }.toTypedArray()
        val selectedIndex = languages.indexOf(settingsStore.appLanguage())
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_language_dialog_title)
            .setSingleChoiceItems(labels, selectedIndex) { dialog, which ->
                dialog.dismiss()
                settingsStore.setAppLanguage(languages[which])
            }
            .show()
    }

    private fun refreshCacheSize() {
        lifecycleScope.launch {
            val cacheSize = withContext(Dispatchers.IO) {
                CacheUtils.getTotalCacheSize(this@SettingsActivity)
            }
            cacheValueView.text = CacheUtils.formatSize(cacheSize)
        }
    }

    private fun buildVersionLabel(): String {
        return runCatching {
            "v${resolveVersionName()} (${resolveVersionCode()})"
        }.getOrDefault("v1.0 (1)")
    }

    private fun resolveVersionName(): String {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return packageInfo.versionName.orEmpty().ifBlank { "1.0" }
    }

    private fun resolveVersionCode(): Long {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    }

    companion object {
        private const val AUTHOR_NAME = "2yh"
        private const val AUTHOR_EMAIL = "androidalex@126.com"

        fun createIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
