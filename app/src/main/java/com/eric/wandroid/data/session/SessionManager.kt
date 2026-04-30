package com.eric.wandroid.data.session

import com.eric.wandroid.domain.model.UserSession
import kotlinx.coroutines.flow.StateFlow
import okhttp3.Cookie
import okhttp3.HttpUrl

interface SessionManager {
    val sessionState: StateFlow<UserSession?>

    fun saveCookies(url: HttpUrl, cookies: List<Cookie>)

    fun loadCookies(url: HttpUrl): List<Cookie>

    fun saveSession(session: UserSession)

    fun currentSession(): UserSession?

    fun clearSession()

    fun isLoggedIn(): Boolean = currentSession() != null
}
