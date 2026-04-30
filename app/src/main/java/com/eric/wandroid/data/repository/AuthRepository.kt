package com.eric.wandroid.data.repository

import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.mapper.toSession
import com.eric.wandroid.data.remote.api.WanAndroidApiService
import com.eric.wandroid.data.remote.apiErrorCode
import com.eric.wandroid.data.remote.requireData
import com.eric.wandroid.data.remote.requireSuccess
import com.eric.wandroid.data.remote.toRepositoryMessage
import com.eric.wandroid.data.session.SessionManager
import com.eric.wandroid.domain.model.UserSession
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(
    private val apiService: WanAndroidApiService,
    private val sessionStore: SessionManager
) {
    val sessionState: StateFlow<UserSession?> = sessionStore.sessionState

    fun isLoggedIn(): Boolean = sessionStore.isLoggedIn()

    fun currentSession(): UserSession? = sessionStore.currentSession()

    suspend fun login(username: String, password: String): AppResult<UserSession> {
        return try {
            val user = apiService.login(username.trim(), password).requireData()
            val session = user.toSession()
            sessionStore.saveSession(session)
            AppResult.Success(session)
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    suspend fun register(
        username: String,
        password: String,
        repeatPassword: String
    ): AppResult<UserSession> {
        return try {
            val user = apiService.register(username.trim(), password, repeatPassword).requireData()
            val session = user.toSession()
            sessionStore.saveSession(session)
            AppResult.Success(session)
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    suspend fun logout(): AppResult<Unit> {
        return try {
            apiService.logout().requireSuccess()
            sessionStore.clearSession()
            AppResult.Success(Unit)
        } catch (exception: Throwable) {
            sessionStore.clearSession()
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }
}
