package com.eric.wandroid.data.repository

import android.util.Log
import com.eric.wandroid.common.auth.LOGIN_EXPIRED_CODE
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.mapper.toDomain
import com.eric.wandroid.data.remote.api.WanAndroidApiService
import com.eric.wandroid.data.remote.apiErrorCode
import com.eric.wandroid.data.remote.requireData
import com.eric.wandroid.data.remote.requireSuccess
import com.eric.wandroid.data.remote.toRepositoryMessage
import com.eric.wandroid.data.session.SessionManager
import com.eric.wandroid.domain.model.CoinOverview
import com.eric.wandroid.domain.model.CoinRecordPageData
import com.eric.wandroid.domain.model.CollectedArticlePageData
import com.eric.wandroid.domain.model.MessageOverview
import com.eric.wandroid.domain.model.TodoPageData
import com.eric.wandroid.domain.model.UserMessagePageData
import com.eric.wandroid.domain.model.UserProfile

class AccountRepository(
    private val apiService: WanAndroidApiService,
    private val sessionManager: SessionManager
) {
    suspend fun loadUserProfile(): AppResult<UserProfile> {
        val notLoggedInError = requireLoggedIn("loadUserProfile")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            val profile = apiService.getUserInfo().requireData().toDomain()
            sessionManager.currentSession()?.let { session ->
                sessionManager.saveSession(
                    session.copy(
                        username = profile.username,
                        displayName = profile.displayName,
                        email = profile.email
                    )
                )
            }
            AppResult.Success(profile)
        } catch (exception: Throwable) {
            exception.toAppError("loadUserProfile")
        }
    }

    suspend fun loadCollectedArticles(page: Int): AppResult<CollectedArticlePageData> {
        val notLoggedInError = requireLoggedIn("loadCollectedArticles(page=$page)")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            val pageDto = apiService.getCollectedArticles(page).requireData()
            AppResult.Success(
                CollectedArticlePageData(
                    page = page,
                    articles = pageDto.datas.orEmpty().map { it.toDomain() },
                    canLoadMore = pageDto.over != true
                )
            )
        } catch (exception: Throwable) {
            exception.toAppError("loadCollectedArticles(page=$page)")
        }
    }

    suspend fun loadCoinOverview(): AppResult<CoinOverview> {
        val notLoggedInError = requireLoggedIn("loadCoinOverview")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            val overview = apiService.getCoinUserInfo().requireData().toDomain(sessionManager.currentSession())
            AppResult.Success(overview)
        } catch (exception: Throwable) {
            exception.toAppError("loadCoinOverview")
        }
    }

    suspend fun loadCoinRecords(page: Int): AppResult<CoinRecordPageData> {
        val notLoggedInError = requireLoggedIn("loadCoinRecords(page=$page)")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            val pageDto = apiService.getCoinRecords(page).requireData()
            AppResult.Success(
                CoinRecordPageData(
                    page = pageDto.curPage ?: page,
                    records = pageDto.datas.orEmpty().map { it.toDomain() },
                    canLoadMore = pageDto.over != true
                )
            )
        } catch (exception: Throwable) {
            exception.toAppError("loadCoinRecords(page=$page)")
        }
    }

    suspend fun loadMessageOverview(): AppResult<MessageOverview> {
        val notLoggedInError = requireLoggedIn("loadMessageOverview")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            val unreadCount = apiService.getUnreadMessageCount().requireData() ?: 0
            AppResult.Success(MessageOverview(unreadCount = unreadCount))
        } catch (exception: Throwable) {
            exception.toAppError("loadMessageOverview")
        }
    }

    suspend fun loadUnreadMessages(page: Int): AppResult<UserMessagePageData> {
        val notLoggedInError = requireLoggedIn("loadUnreadMessages(page=$page)")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            val pageDto = apiService.getUnreadMessages(page).requireData()
            AppResult.Success(
                UserMessagePageData(
                    page = pageDto.curPage ?: page,
                    messages = pageDto.datas.orEmpty().map { it.toDomain(defaultIsRead = false) },
                    canLoadMore = pageDto.over != true
                )
            )
        } catch (exception: Throwable) {
            exception.toAppError("loadUnreadMessages(page=$page)")
        }
    }

    suspend fun loadReadMessages(page: Int): AppResult<UserMessagePageData> {
        val notLoggedInError = requireLoggedIn("loadReadMessages(page=$page)")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            val pageDto = apiService.getReadMessages(page).requireData()
            AppResult.Success(
                UserMessagePageData(
                    page = pageDto.curPage ?: page,
                    messages = pageDto.datas.orEmpty().map { it.toDomain(defaultIsRead = true) },
                    canLoadMore = pageDto.over != true
                )
            )
        } catch (exception: Throwable) {
            exception.toAppError("loadReadMessages(page=$page)")
        }
    }

    suspend fun loadTodos(page: Int, status: Int? = null): AppResult<TodoPageData> {
        val notLoggedInError = requireLoggedIn("loadTodos(page=$page,status=$status)")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            val pageDto = apiService.getTodos(page = page, status = status).requireData()
            AppResult.Success(
                TodoPageData(
                    page = pageDto.curPage ?: page,
                    todos = pageDto.datas.orEmpty().map { it.toDomain() },
                    canLoadMore = pageDto.over != true
                )
            )
        } catch (exception: Throwable) {
            exception.toAppError("loadTodos(page=$page,status=$status)")
        }
    }

    suspend fun addTodo(
        title: String,
        content: String,
        date: String,
        type: Int,
        priority: Int
    ): AppResult<Unit> {
        val notLoggedInError = requireLoggedIn("addTodo")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            apiService.addTodo(title, content, date, type, priority).requireSuccess()
            AppResult.Success(Unit)
        } catch (exception: Throwable) {
            exception.toAppError("addTodo")
        }
    }

    suspend fun updateTodo(
        id: Int,
        title: String,
        content: String,
        date: String,
        type: Int,
        priority: Int
    ): AppResult<Unit> {
        val notLoggedInError = requireLoggedIn("updateTodo(id=$id)")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            apiService.updateTodo(id, title, content, date, type, priority).requireSuccess()
            AppResult.Success(Unit)
        } catch (exception: Throwable) {
            exception.toAppError("updateTodo(id=$id)")
        }
    }

    suspend fun deleteTodo(id: Int): AppResult<Unit> {
        val notLoggedInError = requireLoggedIn("deleteTodo(id=$id)")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            apiService.deleteTodo(id).requireSuccess()
            AppResult.Success(Unit)
        } catch (exception: Throwable) {
            exception.toAppError("deleteTodo(id=$id)")
        }
    }

    suspend fun updateTodoStatus(id: Int, status: Int): AppResult<Unit> {
        val notLoggedInError = requireLoggedIn("updateTodoStatus(id=$id,status=$status)")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            apiService.updateTodoStatus(id, status).requireSuccess()
            AppResult.Success(Unit)
        } catch (exception: Throwable) {
            exception.toAppError("updateTodoStatus(id=$id,status=$status)")
        }
    }

    suspend fun collectArticle(articleId: Int): AppResult<Unit> {
        val notLoggedInError = requireLoggedIn("collectArticle(articleId=$articleId)")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            apiService.collectArticle(articleId).requireSuccess()
            AppResult.Success(Unit)
        } catch (exception: Throwable) {
            exception.toAppError("collectArticle(articleId=$articleId)")
        }
    }

    suspend fun uncollectArticle(articleId: Int): AppResult<Unit> {
        val notLoggedInError = requireLoggedIn("uncollectArticle(articleId=$articleId)")
        if (notLoggedInError != null) return notLoggedInError

        return try {
            apiService.uncollectArticle(articleId).requireSuccess()
            AppResult.Success(Unit)
        } catch (exception: Throwable) {
            exception.toAppError("uncollectArticle(articleId=$articleId)")
        }
    }

    private fun requireLoggedIn(operation: String): AppResult.Error? {
        if (sessionManager.isLoggedIn()) return null
        Log.w(
            TAG,
            "blockedBeforeRequest operation=$operation reason=no_local_session ${buildSessionDebugInfo()}"
        )
        return AppResult.Error(message = "Login expired. Please sign in again.", code = LOGIN_EXPIRED_CODE)
    }

    private fun Throwable.toAppError(operation: String): AppResult.Error {
        val code = apiErrorCode()
        if (code == LOGIN_EXPIRED_CODE) {
            Log.w(
                TAG,
                "loginExpired operation=$operation reason=server_error code=$code message=${toRepositoryMessage()} ${buildSessionDebugInfo()}"
            )
            sessionManager.clearSession()
        }
        return AppResult.Error(toRepositoryMessage(), code)
    }

    private fun buildSessionDebugInfo(): String {
        val session = sessionManager.currentSession()
        val sessionSummary = if (session == null) {
            "session=null"
        } else {
            "session=userId=${session.id},username=${session.username}"
        }
        val cookieSummary = (sessionManager as? com.eric.wandroid.data.session.SessionStore)
            ?.debugCookieSummary(WANANDROID_HOST)
            ?: "cookies=unavailable"
        return "$sessionSummary $cookieSummary"
    }

    companion object {
        private const val TAG = "AccountRepository"
        private const val WANANDROID_HOST = "www.wanandroid.com"
    }
}
