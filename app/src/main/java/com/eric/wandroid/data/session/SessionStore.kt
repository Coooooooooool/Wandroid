package com.eric.wandroid.data.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.eric.wandroid.domain.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Cookie
import okhttp3.HttpUrl

class SessionStore(context: Context) : SessionManager {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val _sessionState = MutableStateFlow(readPersistedSession())

    override val sessionState: StateFlow<UserSession?> = _sessionState.asStateFlow()

    override fun saveCookies(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        preferences.edit()
            .putString(cookieKey(url.host), cookies.joinToString(SEPARATOR) { it.toString() })
            .apply()
        Log.d(
            TAG,
            "saveCookies host=${url.host} summary=${buildCookieSummary(cookies)}"
        )
    }

    override fun loadCookies(url: HttpUrl): List<Cookie> {
        val serialized = preferences.getString(cookieKey(url.host), null).orEmpty()
        if (serialized.isBlank()) return emptyList()
        return serialized.split(SEPARATOR).mapNotNull { Cookie.parse(url, it) }
    }

    override fun saveSession(session: UserSession) {
        val sanitizedSession = session.copy(
            username = session.username.trim(),
            displayName = session.displayName.ifBlank { session.username.trim() },
            email = session.email.trim(),
            isLoggedIn = true
        )
        preferences.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putBoolean(KEY_AUTH_CONFIRMED, true)
            .putInt(KEY_USER_ID, sanitizedSession.id)
            .putString(KEY_USERNAME, sanitizedSession.username)
            .putString(KEY_DISPLAY_NAME, sanitizedSession.displayName)
            .putString(KEY_EMAIL, sanitizedSession.email)
            .apply()
        _sessionState.value = sanitizedSession
        Log.i(
            TAG,
            "saveSession userId=${sanitizedSession.id} username=${sanitizedSession.username}"
        )
    }

    override fun currentSession(): UserSession? = readPersistedSession()

    override fun clearSession() {
        val keysToClear = preferences.all.keys.filter { key ->
            key == KEY_LOGGED_IN ||
                key == KEY_AUTH_CONFIRMED ||
                key == KEY_USER_ID ||
                key == KEY_USERNAME ||
                key == KEY_DISPLAY_NAME ||
                key == KEY_EMAIL ||
                key.startsWith(KEY_COOKIE_PREFIX)
        }
        preferences.edit().apply {
            keysToClear.forEach(::remove)
            apply()
        }
        _sessionState.value = null
        Log.w(
            TAG,
            "clearSession removedKeys=${keysToClear.size} cookieKeys=${keysToClear.count { it.startsWith(KEY_COOKIE_PREFIX) }}"
        )
    }

    fun debugCookieSummary(host: String): String {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(host)
            .build()
        return buildCookieSummary(loadCookies(url))
    }

    private fun buildCookieSummary(cookies: List<Cookie>): String {
        if (cookies.isEmpty()) return "count=0"
        return cookies.joinToString(
            prefix = "count=${cookies.size} [",
            postfix = "]"
        ) { cookie ->
            val expiresAt = if (cookie.persistent) cookie.expiresAt.toString() else "session"
            "${cookie.name}=$expiresAt"
        }
    }

    private fun readPersistedSession(): UserSession? {
        val isSessionConfirmed = preferences.getBoolean(KEY_LOGGED_IN, false) &&
            preferences.getBoolean(KEY_AUTH_CONFIRMED, false)
        if (!isSessionConfirmed) return null

        val username = preferences.getString(KEY_USERNAME, null).orEmpty().trim()
        if (username.isBlank()) return null

        return UserSession(
            id = preferences.getInt(KEY_USER_ID, 0),
            username = username,
            displayName = preferences.getString(KEY_DISPLAY_NAME, null)
                .orEmpty()
                .ifBlank { username },
            email = preferences.getString(KEY_EMAIL, null).orEmpty(),
            isLoggedIn = true
        )
    }

    private fun cookieKey(host: String): String = "$KEY_COOKIE_PREFIX$host"

    companion object {
        private const val TAG = "SessionStore"
        private const val PREF_NAME = "wandroid_session"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_AUTH_CONFIRMED = "auth_confirmed"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_COOKIE_PREFIX = "cookies_"
        private const val SEPARATOR = "\n"
    }
}
