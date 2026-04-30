package com.eric.wandroid.data.session

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(
    private val sessionStore: SessionManager
) : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        sessionStore.saveCookies(url, cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return sessionStore.loadCookies(url)
    }
}
