package com.eric.wandroid.common.ui

import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.eric.wandroid.R
import com.google.android.material.appbar.MaterialToolbar

object EdgeToEdgeHelper {
    fun apply(
        activity: AppCompatActivity,
        toolbar: View,
        vararg contentViews: View
    ) {
        styleToolbar(activity, toolbar)
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        activity.window.statusBarColor = Color.TRANSPARENT
        activity.window.navigationBarColor = Color.TRANSPARENT

        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = !activity.isNightMode()
        }

        toolbar.applyInsets(
            types = WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout(),
            applyTopInset = true,
            applyBottomInset = false
        )

        contentViews.forEach { view ->
            view.applyInsets(
                types = WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout(),
                applyTopInset = false,
                applyBottomInset = true
            )
        }

        ViewCompat.requestApplyInsets(activity.window.decorView)
    }

    fun applySurfaceToolbar(
        activity: AppCompatActivity,
        toolbar: View,
        vararg contentViews: View
    ) {
        styleSurfaceToolbar(activity, toolbar)
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        activity.window.statusBarColor = Color.TRANSPARENT
        activity.window.navigationBarColor = Color.TRANSPARENT

        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = !activity.isNightMode()
            isAppearanceLightNavigationBars = !activity.isNightMode()
        }

        toolbar.applyInsets(
            types = WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout(),
            applyTopInset = true,
            applyBottomInset = false
        )

        contentViews.forEach { view ->
            view.applyInsets(
                types = WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout(),
                applyTopInset = false,
                applyBottomInset = true
            )
        }

        ViewCompat.requestApplyInsets(activity.window.decorView)
    }

    fun applyContentOnly(
        activity: AppCompatActivity,
        topContentView: View,
        vararg bottomContentViews: View
    ) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        activity.window.statusBarColor = Color.TRANSPARENT
        activity.window.navigationBarColor = Color.TRANSPARENT

        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = !activity.isNightMode()
            isAppearanceLightNavigationBars = !activity.isNightMode()
        }

        topContentView.applyInsets(
            types = WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout(),
            applyTopInset = true,
            applyBottomInset = false
        )

        bottomContentViews.forEach { view ->
            view.applyInsets(
                types = WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout(),
                applyTopInset = false,
                applyBottomInset = true
            )
        }

        ViewCompat.requestApplyInsets(activity.window.decorView)
    }

    private fun styleToolbar(activity: AppCompatActivity, toolbar: View) {
        val toolbarBackground = ContextCompat.getColor(activity, R.color.brand_primary)
        val toolbarForeground = ContextCompat.getColor(activity, R.color.white)

        toolbar.setBackgroundColor(toolbarBackground)
        if (toolbar is MaterialToolbar) {
            toolbar.setTitleTextColor(toolbarForeground)
            toolbar.setSubtitleTextColor(toolbarForeground)
            toolbar.navigationIcon?.setTint(toolbarForeground)
            toolbar.overflowIcon?.setTint(toolbarForeground)
        }
    }

    private fun styleSurfaceToolbar(activity: AppCompatActivity, toolbar: View) {
        val toolbarBackground = ContextCompat.getColor(activity, R.color.surface_background)
        val toolbarForeground = ContextCompat.getColor(activity, R.color.text_primary)

        toolbar.setBackgroundColor(toolbarBackground)
        if (toolbar is MaterialToolbar) {
            toolbar.setTitleTextColor(toolbarForeground)
            toolbar.setSubtitleTextColor(toolbarForeground)
            toolbar.navigationIcon?.setTint(toolbarForeground)
            toolbar.overflowIcon?.setTint(toolbarForeground)
        }
    }

    private fun View.applyInsets(
        types: Int,
        applyTopInset: Boolean,
        applyBottomInset: Boolean
    ) {
        val initialPadding = recordInitialPadding()
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val insets = windowInsets.getInsets(types)
            view.updatePadding(
                left = initialPadding.left + insets.left,
                top = initialPadding.top + if (applyTopInset) insets.top else 0,
                right = initialPadding.right + insets.right,
                bottom = initialPadding.bottom + if (applyBottomInset) insets.bottom else 0
            )
            windowInsets
        }
    }

    private fun View.recordInitialPadding(): Insets {
        return Insets.of(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }

    private fun AppCompatActivity.isNightMode(): Boolean {
        return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    }
}
