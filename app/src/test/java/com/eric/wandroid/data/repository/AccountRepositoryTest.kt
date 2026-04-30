package com.eric.wandroid.data.repository

import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.api.WanAndroidApiService
import com.eric.wandroid.data.remote.dto.ApiResponse
import com.eric.wandroid.data.remote.dto.ArticleDto
import com.eric.wandroid.data.remote.dto.BannerDto
import com.eric.wandroid.data.remote.dto.ChapterDto
import com.eric.wandroid.data.remote.dto.CoinInfoDto
import com.eric.wandroid.data.remote.dto.CoinRecordDto
import com.eric.wandroid.data.remote.dto.CoinUserInfoDto
import com.eric.wandroid.data.remote.dto.HotKeyDto
import com.eric.wandroid.data.remote.dto.NavigationDto
import com.eric.wandroid.data.remote.dto.PageDto
import com.eric.wandroid.data.remote.dto.PopularColumnDto
import com.eric.wandroid.data.remote.dto.TodoDto
import com.eric.wandroid.data.remote.dto.UserMessageDto
import com.eric.wandroid.data.remote.dto.UserDto
import com.eric.wandroid.data.remote.dto.UserInfoDetailDto
import com.eric.wandroid.data.remote.dto.UserInfoResponseDto
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
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountRepositoryTest {
    @Test
    fun loadUserProfile_mapsCoinAndUserInfoAndRefreshesSession() = runBlocking {
        val sessionManager = FakeAccountSessionManager(
            UserSession(
                id = 1,
                username = "alice",
                displayName = "Alice",
                email = "",
                isLoggedIn = true
            )
        )
        val repository = AccountRepository(
            apiService = FakeAccountApiService(
                userInfoResponse = ApiResponse(
                    data = UserInfoResponseDto(
                        coinInfo = CoinInfoDto(
                            coinCount = 88,
                            level = 5,
                            rank = "12",
                            userId = 1,
                            username = "alice",
                            nickname = "Alice Coin"
                        ),
                        userInfo = UserInfoDetailDto(
                            id = 1,
                            username = "alice",
                            nickname = "Alice",
                            publicName = "",
                            email = "alice@example.com",
                            coinCount = 99,
                            icon = null,
                            type = 0,
                            admin = false
                        )
                    ),
                    errorCode = 0,
                    errorMsg = ""
                )
            ),
            sessionManager = sessionManager
        )

        val result = repository.loadUserProfile()

        assertTrue(result is AppResult.Success)
        val profile = (result as AppResult.Success).data
        assertEquals("Alice", profile.displayName)
        assertEquals(99, profile.coinCount)
        assertEquals(5, profile.level)
        assertEquals("12", profile.rank)
        assertEquals("alice@example.com", sessionManager.currentSession()?.email)
    }

    @Test
    fun collectArticle_returnsNotLoggedInWithoutNetworkCall() = runBlocking {
        val apiService = FakeAccountApiService()
        val repository = AccountRepository(
            apiService = apiService,
            sessionManager = FakeAccountSessionManager()
        )

        val result = repository.collectArticle(100)

        assertTrue(result is AppResult.Error)
        assertFalse(apiService.collectCalled)
    }

    @Test
    fun loadCollectedArticles_mapsArticlesAndPagination() = runBlocking {
        val repository = AccountRepository(
            apiService = FakeAccountApiService(
                collectedArticlesResponse = ApiResponse(
                    data = PageDto(
                        curPage = 1,
                        datas = listOf(
                            ArticleDto(
                                id = 3,
                                title = "Title",
                                link = "https://example.com",
                                author = "Author",
                                shareUser = "",
                                chapterName = "Chapter",
                                superChapterName = "Super",
                                niceDate = "today",
                                publishTime = null,
                                collect = true,
                                envelopePic = null,
                                desc = "Desc"
                            )
                        ),
                        offset = 0,
                        over = false,
                        pageCount = 2,
                        size = 20,
                        total = 21
                    ),
                    errorCode = 0,
                    errorMsg = ""
                )
            ),
            sessionManager = FakeAccountSessionManager(
                UserSession(1, "alice", "Alice", "", true)
            )
        )

        val result = repository.loadCollectedArticles(0)

        assertTrue(result is AppResult.Success)
        val page = (result as AppResult.Success).data
        assertEquals(1, page.articles.size)
        assertTrue(page.articles.first().isCollected)
        assertTrue(page.canLoadMore)
    }

    @Test
    fun loadCoinOverview_usesCoinEndpointAndSessionDisplayName() = runBlocking {
        val repository = AccountRepository(
            apiService = FakeAccountApiService(
                coinUserInfoResponse = ApiResponse(
                    data = CoinUserInfoDto(
                        coinCount = 321,
                        rank = 9,
                        userId = 7,
                        username = "alice"
                    ),
                    errorCode = 0,
                    errorMsg = ""
                )
            ),
            sessionManager = FakeAccountSessionManager(
                UserSession(7, "alice", "Alice", "", true)
            )
        )

        val result = repository.loadCoinOverview()

        assertTrue(result is AppResult.Success)
        val overview = (result as AppResult.Success).data
        assertEquals("Alice", overview.displayName)
        assertEquals(321, overview.coinCount)
        assertEquals(9, overview.rank)
    }

    @Test
    fun loadCoinRecords_mapsRecordsAndPagination() = runBlocking {
        val repository = AccountRepository(
            apiService = FakeAccountApiService(
                coinRecordsResponse = ApiResponse(
                    data = PageDto(
                        curPage = 1,
                        datas = listOf(
                            CoinRecordDto(
                                coinCount = 5,
                                date = 1713800000000L,
                                desc = "Daily sign-in",
                                id = 11,
                                reason = "Sign in",
                                type = 1,
                                userId = 7,
                                userName = "alice"
                            )
                        ),
                        offset = 0,
                        over = false,
                        pageCount = 2,
                        size = 20,
                        total = 21
                    ),
                    errorCode = 0,
                    errorMsg = ""
                )
            ),
            sessionManager = FakeAccountSessionManager(
                UserSession(7, "alice", "Alice", "", true)
            )
        )

        val result = repository.loadCoinRecords(page = 1)

        assertTrue(result is AppResult.Success)
        val page = (result as AppResult.Success).data
        assertEquals(1, page.page)
        assertEquals(1, page.records.size)
        assertEquals(5, page.records.first().coinCount)
        assertTrue(page.canLoadMore)
    }
}

private class FakeAccountSessionManager(
    initialSession: UserSession? = null
) : SessionManager {
    private val current = MutableStateFlow(initialSession)

    override val sessionState: StateFlow<UserSession?> = current

    override fun saveCookies(url: HttpUrl, cookies: List<Cookie>) = Unit

    override fun loadCookies(url: HttpUrl): List<Cookie> = emptyList()

    override fun saveSession(session: UserSession) {
        current.value = session
    }

    override fun currentSession(): UserSession? = current.value

    override fun clearSession() {
        current.value = null
    }
}

private class FakeAccountApiService(
    private val userInfoResponse: ApiResponse<UserInfoResponseDto> = unsupportedUserInfoResponse(),
    private val collectedArticlesResponse: ApiResponse<PageDto<ArticleDto>> = unsupportedCollectedArticlesResponse(),
    private val coinUserInfoResponse: ApiResponse<CoinUserInfoDto> = unsupportedCoinUserInfoResponse(),
    private val coinRecordsResponse: ApiResponse<PageDto<CoinRecordDto>> = unsupportedCoinRecordsResponse(),
    private val collectResponse: ApiResponse<Any> = ApiResponse(data = null, errorCode = 0, errorMsg = ""),
    private val uncollectResponse: ApiResponse<Any> = ApiResponse(data = null, errorCode = 0, errorMsg = "")
) : WanAndroidApiService {
    var collectCalled: Boolean = false
        private set

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

    override suspend fun getSystemArticles(page: Int, categoryId: Int): ApiResponse<PageDto<ArticleDto>> = unsupported()

    override suspend fun getProjectCategories(): ApiResponse<List<ChapterDto>> = unsupported()

    override suspend fun getProjectArticles(page: Int, categoryId: Int): ApiResponse<PageDto<ArticleDto>> = unsupported()

    override suspend fun searchArticles(page: Int, keyword: String): ApiResponse<PageDto<ArticleDto>> = unsupported()

    override suspend fun login(username: String, password: String): ApiResponse<UserDto> = unsupported()

    override suspend fun register(
        username: String,
        password: String,
        repeatPassword: String
    ): ApiResponse<UserDto> = unsupported()

    override suspend fun logout(): ApiResponse<Any> = unsupported()

    override suspend fun getUserInfo(): ApiResponse<UserInfoResponseDto> = userInfoResponse

    override suspend fun collectArticle(id: Int): ApiResponse<Any> {
        collectCalled = true
        return collectResponse
    }

    override suspend fun uncollectArticle(id: Int): ApiResponse<Any> = uncollectResponse

    override suspend fun getCollectedArticles(page: Int): ApiResponse<PageDto<ArticleDto>> = collectedArticlesResponse

    override suspend fun getWendaArticles(page: Int): ApiResponse<PageDto<ArticleDto>> = unsupported()

    override suspend fun getSquareArticles(page: Int): ApiResponse<PageDto<ArticleDto>> = unsupported()

    override suspend fun getWechatCategories(): ApiResponse<List<ChapterDto>> = unsupported()

    override suspend fun getWechatArticles(categoryId: Int, page: Int): ApiResponse<PageDto<ArticleDto>> = unsupported()

    override suspend fun getLatestProjectArticles(page: Int): ApiResponse<PageDto<ArticleDto>> = unsupported()

    override suspend fun getCoinUserInfo(): ApiResponse<CoinUserInfoDto> = coinUserInfoResponse

    override suspend fun getCoinRecords(page: Int): ApiResponse<PageDto<CoinRecordDto>> = coinRecordsResponse

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
        throw UnsupportedOperationException("Unused in account repository tests.")
    }
}

private fun unsupportedUserInfoResponse(): ApiResponse<UserInfoResponseDto> {
    return ApiResponse(data = null, errorCode = -1, errorMsg = "Unsupported test call.")
}

private fun unsupportedCollectedArticlesResponse(): ApiResponse<PageDto<ArticleDto>> {
    return ApiResponse(data = null, errorCode = -1, errorMsg = "Unsupported test call.")
}

private fun unsupportedCoinUserInfoResponse(): ApiResponse<CoinUserInfoDto> {
    return ApiResponse(data = null, errorCode = -1, errorMsg = "Unsupported test call.")
}

private fun unsupportedCoinRecordsResponse(): ApiResponse<PageDto<CoinRecordDto>> {
    return ApiResponse(data = null, errorCode = -1, errorMsg = "Unsupported test call.")
}
