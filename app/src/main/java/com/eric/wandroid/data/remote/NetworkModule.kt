package com.eric.wandroid.data.remote

import android.content.Context
import com.eric.wandroid.data.remote.api.MoyuApiService
import com.eric.wandroid.data.remote.api.MoeHuApiService
import com.eric.wandroid.data.remote.api.InternetNewsApiService
import com.eric.wandroid.data.remote.api.NewsApiService
import com.eric.wandroid.data.remote.api.WanAndroidApiService
import com.eric.wandroid.data.session.PersistentCookieJar
import com.eric.wandroid.data.session.SessionStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "https://www.wanandroid.com/"
    private const val MOYU_BASE_URL = "https://api.kuleu.com/"
    private const val NEWS_BASE_URL = "https://v.juhe.cn/"
    private const val MOEHU_BASE_URL = "https://img.moehu.org/"

    private var appContext: Context? = null
    private var sessionStore: SessionStore? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        if (sessionStore == null) {
            sessionStore = SessionStore(context.applicationContext)
        }
    }

    fun requireSessionStoreContext(): Context {
        return appContext ?: error("NetworkModule is not initialized.")
    }

    fun requireSessionStore(): SessionStore {
        return sessionStore ?: error("NetworkModule is not initialized.")
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .cookieJar(PersistentCookieJar(requireSessionStore()))
            .addInterceptor(loggingInterceptor)
            .build()
    }

    val apiService: WanAndroidApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WanAndroidApiService::class.java)
    }

    val moyuApiService: MoyuApiService by lazy {
        Retrofit.Builder()
            .baseUrl(MOYU_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MoyuApiService::class.java)
    }

    val newsApiService: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NEWS_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }

    val internetNewsApiService: InternetNewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://apis.juhe.cn/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InternetNewsApiService::class.java)
    }

    val moeHuApiService: MoeHuApiService by lazy {
        Retrofit.Builder()
            .baseUrl(MOEHU_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MoeHuApiService::class.java)
    }
}
