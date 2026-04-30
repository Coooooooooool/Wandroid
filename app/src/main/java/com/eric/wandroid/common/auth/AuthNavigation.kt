package com.eric.wandroid.common.auth

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.eric.wandroid.ui.auth.AuthActivity
import com.eric.wandroid.ui.home.HomeActivity
import com.eric.wandroid.ui.shell.MainTab

const val LOGIN_EXPIRED_CODE = -1001

fun FragmentActivity.navigateToLogin(message: String, finishCurrent: Boolean = false) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    startActivity(AuthActivity.createIntent(this))
    if (finishCurrent) {
        finish()
    }
}

fun Context.createHomeIntent(initialTab: MainTab = MainTab.Home): Intent {
    return HomeActivity.createIntent(this, initialTab)
}
