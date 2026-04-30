package com.eric.wandroid.data.repository

import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.api.WanAndroidApiService
import com.eric.wandroid.data.remote.dto.ApiResponse
import com.eric.wandroid.data.remote.dto.ArticleDto
import com.eric.wandroid.data.remote.dto.BannerDto
import com.eric.wandroid.data.remote.dto.ChapterDto
import com.eric.wandroid.data.remote.dto.CoinRecordDto
import com.eric.wandroid.data.remote.dto.CoinUserInfoDto
import com.eric.wandroid.data.remote.dto.HotKeyDto
import com.eric.wandroid.data.remote.dto.NavigationDto
import com.eric.wandroid.data.remote.dto.PageDto
import com.eric.wandroid.data.remote.dto.PopularColumnDto
import com.eric.wandroid.data.remote.dto.TodoDto
import com.eric.wandroid.data.remote.dto.UserMessageDto
import com.eric.wandroid.data.remote.dto.UserInfoResponseDto
import com.eric.wandroid.data.remote.dto.UserDto
import com.eric.wandroid.data.remote.dto.WebsiteDto
import com.eric.wandroid.data.session.SessionManager
import com.eric.wandroid.domain.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.HttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {
    @Test
    fun login_savesSessionAndUpdatesState() = runBlocking {
        val sessionManager = FakeSessionManager()
        val repository = AuthRepository(
            apiService = FakeWanAndroidApiService(
                loginResponse = ApiResponse(
                    data = UserDto(
                        id = 7,
                        username = "alice",
                        nickname = "Alice",
                        publicName = null,
                        email = "alice@example.com",
                        icon = null,
                        type = 0,
                        admin = false
                    ),
                    errorCode = 0,
                    errorMsg = ""
                )
            ),
            sessionStore = sessionManager
        )

        val result = repository.login("alice", "secret")

        assertTrue(result is AppResult.Success)
        assertEquals("alice", repository.currentSession()?.username)
        assertEquals("Alice", repository.currentSession()?.displayName)
        assertEquals("alice@example.com", repository.currentSession()?.email)
        assertTrue(repository.isLoggedIn())
        assertEquals("Alice", repository.sessionState.value?.displayName)
    }

    @Test
    fun register_savesSessionWithFallbackDisplayName() = runBlocking {
        val sessionManager = FakeSessionManager()
        val repository = AuthRepository(
            apiService = FakeWanAndroidApiService(
                registerResponse = ApiResponse(
                    data = UserDto(
                        id = 9,
                        username = "bob",
                        nickname = "",
                        publicName = "",
                        email = "",
                        icon = null,
                        type = 0,
                        admin = false
                    ),
                    errorCode = 0,
                    errorMsg = ""
                )
            ),
            sessionStore = sessionManager
        )

        val result = repository.register("bob", "secret", "secret")

        assertTrue(result is AppResult.Success)
        assertEquals("bob", repository.currentSession()?.displayName)
        assertEquals("bob", sessionManager.currentSession()?.username)
        assertNotNull(repository.sessionState.value)
    }

    @Test
    fun logout_clearsSessionEvenWhenServerRejectsRequest() = runBlocking {
        val initialSession = UserSession(
            id = 11,
            username = "carol",
            displayName = "Carol",
            email = "carol@example.com",
            isLoggedIn = true
        )
        val sessionManager = FakeSessionManager(initialSession)
        val repository = AuthRepository(
            apiService = FakeWanAndroidApiService(
                logoutResponse = ApiResponse(
                    data = null,
                    errorCode = -1001,
                    errorMsg = "Login expired. Please sign in again."
                )
            ),
            sessionStore = sessionManager
        )

        val result = repository.logout()

        assertTrue(result is AppResult.Error)
        assertNull(repository.currentSession())
        assertNull(repository.sessionState.value)
        assertFalse(repository.isLoggedIn())
    }
}

private class FakeSessionManager(
    initialSession: UserSession? = null
) : SessionManager {
    private val current = MutableStateFlow(initialSession)

    override val sessionState: StateFlow<UserSession?> = current

    override fun saveCookies(url: HttpUrl, cookies: List<Cookie>) = Unit

    override fun loadCookies(url: HttpUrl): List<Cookie> = emptyList()

    override fun saveSession(session: UserSession) {
        current.value = session.copy(isLoggedIn = true)
    }

    override fun currentSession(): UserSession? = current.value

    override fun clearSession() {
        current.value = null
    }
}

private class FakeWanAndroidApiService(
    private val loginResponse: ApiResponse<UserDto> = unsupportedAuthResponse(),
    private val registerResponse: ApiResponse<UserDto> = unsupportedAuthResponse(),
    private val logoutResponse: ApiResponse<Any> = ApiResponse(data = Any(), errorCode = 0, errorMsg = "")
) : WanAndroidApiService {
    override suspend fun getHomeArticles(page: Int): ApiResponse<PageDto<ArticleDto>> = unsupported()

    override suspend fun getTopArticles(): ApiResponse<List<ArticleDto>> = unsupported()

    override suspend fun getBanners(): ApiResponse<List<BannerDto>> = unsupported()

    override suspend fun getHotKeys(): ApiResponse<List<HotKeyDto>> = unsupported()

    override suspend fun getCommonWebsites(): ApiResponse<List<WebsiteDto>> = unsupported()

    override suspend fun getPopularWenda(): ApiResponse<List<ArticleDto>> = unsupported()

    override suspend fun getPopularColumns(): ApiResponse<List<PopularColumnDto>> = unsupported()

    override suspend fun getPopularRoutes(): ApiResponse<List<ChapterDto>> = unsupported()

    override suspend fun getSystemTree(): ApiResponse<List<ChapterDto>> = unsupported()

    override suspend fun getNavigationItems(): ApiResponse<List<NavigationDto>> = unsupported()

    override suspend fun getSystemArticles(page: Int, categoryId: Int): ApiResponse<PageDto<ArticleDto>> =
        unsupported()

    override suspend fun getProjectCategories(): ApiResponse<List<ChapterDto>> = unsupported()

    override suspend fun getProjectArticles(page: Int, categoryId: Int): ApiResponse<PageDto<ArticleDto>> =
        unsupported()

    override suspend fun searchArticles(page: Int, keyword: String): ApiResponse<PageDto<ArticleDto>> =
        unsupported()

    override suspend fun login(username: String, password: String): ApiResponse<UserDto> = loginResponse

    override suspend fun register(
        username: String,
        password: String,
        repeatPassword: String
    ): ApiResponse<UserDto> = registerResponse

    override suspend fun logout(): ApiResponse<Any> = logoutResponse

    override suspend fun getUserInfo(): ApiResponse<UserInfoResponseDto> = unsupported()

    override suspend fun collectArticle(id: Int): ApiResponse<Any> = unsupported()

    override suspend fun uncollectArticle(id: Int): ApiResponse<Any> = unsupported()

    override suspend fun getCollectedArticles(page: Int): ApiResponse<PageDto<ArticleDto>> = unsupported()

    override suspend fun getWendaArticles(page: Int): ApiResponse<PageDto<ArticleDto>> = unsupported()

    override suspend fun getSquareArticles(page: Int): ApiResponse<PageDto<ArticleDto>> = unsupported()

    override suspend fun getWechatCategories(): ApiResponse<List<ChapterDto>> = unsupported()

    override suspend fun getWechatArticles(categoryId: Int, page: Int): ApiResponse<PageDto<ArticleDto>> = unsupported()

    override suspend fun getLatestProjectArticles(page: Int): ApiResponse<PageDto<ArticleDto>> = unsupported()

    override suspend fun getCoinUserInfo(): ApiResponse<CoinUserInfoDto> = unsupported()

    override suspend fun getCoinRecords(page: Int): ApiResponse<PageDto<CoinRecordDto>> = unsupported()

    override suspend fun getUnreadMessageCount(): ApiResponse<Int> = unsupported()

    override suspend fun getUnreadMessages(page: Int): ApiResponse<PageDto<UserMessageDto>> = unsupported()

    override suspend fun getReadMessages(page: Int): ApiResponse<PageDto<UserMessageDto>> = unsupported()

    override suspend fun getTodos(page: Int, status: Int?): ApiResponse<PageDto<TodoDto>> = unsupported()

    override suspend fun addTodo(
        title: String,
        content: String,
        date: String,
        type: Int,
        priority: Int
    ): ApiResponse<TodoDto> = unsupported()

    override suspend fun updateTodo(
        id: Int,
        title: String,
        content: String,
        date: String,
        type: Int,
        priority: Int
    ): ApiResponse<TodoDto> = unsupported()

    override suspend fun deleteTodo(id: Int): ApiResponse<Any> = unsupported()

    override suspend fun updateTodoStatus(id: Int, status: Int): ApiResponse<TodoDto> = unsupported()

    private fun <T> unsupported(): ApiResponse<T> {
        throw UnsupportedOperationException("Unused in auth repository tests.")
    }
}

private fun unsupportedAuthResponse(): ApiResponse<UserDto> {
    return ApiResponse(data = null, errorCode = -1, errorMsg = "Unsupported test call.")
}
