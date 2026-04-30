package com.eric.wandroid.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.auth.LOGIN_EXPIRED_CODE
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.history.HistoryRepository
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.AccountRepository
import com.eric.wandroid.data.repository.HomeRepository
import com.eric.wandroid.data.repository.ProjectSearchRepository
import com.eric.wandroid.domain.model.Article
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val homeRepository: HomeRepository,
    private val projectSearchRepository: ProjectSearchRepository,
    private val accountRepository: AccountRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _authRequired = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val authRequired: SharedFlow<String> = _authRequired.asSharedFlow()

    init {
        loadHotKeys(isRefresh = false)
    }

    fun refresh() {
        if (isBusy()) return
        val state = _uiState.value
        if (state.isResultMode) {
            submitSearch(state.keyword)
        } else {
            loadHotKeys(isRefresh = true)
        }
    }

    fun retry() {
        if (isBusy()) return
        val state = _uiState.value
        if (state.isResultMode) {
            submitSearch(state.keyword)
        } else {
            loadHotKeys(isRefresh = false)
        }
    }

    fun submitSearch(keyword: String) {
        val normalizedKeyword = keyword.trim()
        if (isBusy() || normalizedKeyword.isBlank()) return

        historyRepository.saveSearchKeyword(normalizedKeyword)

        _uiState.update {
            it.copy(
                keyword = normalizedKeyword,
                isInitialLoading = true,
                isRefreshing = false,
                isLoadingMore = false,
                blockingErrorMessage = null,
                currentPage = ProjectSearchRepository.SEARCH_FIRST_PAGE,
                canLoadMore = true,
                searchHistory = historyRepository.getSearchHistory(),
                articles = emptyList()
            )
        }

        viewModelScope.launch {
            when (val result = projectSearchRepository.searchArticles(normalizedKeyword, ProjectSearchRepository.SEARCH_FIRST_PAGE)) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            currentPage = data.searchPage,
                            canLoadMore = data.canLoadMore,
                            articles = data.articles
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            blockingErrorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun selectHotKey(keyword: String) {
        submitSearch(keyword)
    }

    fun clearSearch() {
        if (isBusy()) return
        _uiState.update {
            it.copy(
                keyword = "",
                currentPage = ProjectSearchRepository.SEARCH_FIRST_PAGE,
                canLoadMore = true,
                searchHistory = historyRepository.getSearchHistory(),
                articles = emptyList(),
                blockingErrorMessage = null
            )
        }
        loadHotKeys(isRefresh = false)
    }

    fun removeSearchHistory(keyword: String) {
        historyRepository.removeSearchKeyword(keyword)
        _uiState.update {
            it.copy(searchHistory = historyRepository.getSearchHistory())
        }
    }

    fun clearSearchHistory() {
        historyRepository.clearSearchHistory()
        _uiState.update {
            it.copy(searchHistory = emptyList())
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (isBusy() || !state.isResultMode || !state.canLoadMore) return

        val nextPage = state.currentPage + 1
        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            when (val result = projectSearchRepository.searchArticles(state.keyword, nextPage)) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            currentPage = data.searchPage,
                            canLoadMore = data.canLoadMore,
                            articles = it.articles + data.articles
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoadingMore = false) }
                    emitAuthRequiredIfNeeded(result)
                    _messages.tryEmit(result.message)
                }
            }
        }
    }

    fun toggleCollect(article: Article) {
        val state = _uiState.value
        if (state.collectingArticleIds.contains(article.id)) return

        _uiState.update {
            it.copy(collectingArticleIds = it.collectingArticleIds + article.id)
        }
        viewModelScope.launch {
            val result = if (article.isCollected) {
                accountRepository.uncollectArticle(article.id)
            } else {
                accountRepository.collectArticle(article.id)
            }

            when (result) {
                is AppResult.Success -> {
                    _uiState.update { current ->
                        current.copy(
                            articles = current.articles.map { listedArticle ->
                                if (listedArticle.id == article.id) {
                                    listedArticle.copy(isCollected = !article.isCollected)
                                } else {
                                    listedArticle
                                }
                            },
                            collectingArticleIds = current.collectingArticleIds - article.id
                        )
                    }
                    _messages.tryEmit(
                        if (article.isCollected) "Removed from favorites." else "Added to favorites."
                    )
                }
                is AppResult.Error -> {
                    _uiState.update { current ->
                        current.copy(collectingArticleIds = current.collectingArticleIds - article.id)
                    }
                    emitAuthRequiredIfNeeded(result)
                    _messages.tryEmit(result.message)
                }
            }
        }
    }

    private fun loadHotKeys(isRefresh: Boolean) {
        _uiState.update {
            it.copy(
                isInitialLoading = !isRefresh,
                isRefreshing = isRefresh,
                isLoadingMore = false,
                blockingErrorMessage = null
            )
        }

        viewModelScope.launch {
            when (val result = homeRepository.loadHomePage(page = 0)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            hotKeys = result.data.hotKeys,
                            searchHistory = historyRepository.getSearchHistory(),
                            blockingErrorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            searchHistory = historyRepository.getSearchHistory(),
                            blockingErrorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun emitAuthRequiredIfNeeded(error: AppResult.Error) {
        if (error.code == LOGIN_EXPIRED_CODE) {
            _authRequired.tryEmit(error.message)
        }
    }

    private fun isBusy(): Boolean {
        val state = _uiState.value
        return state.isInitialLoading || state.isRefreshing || state.isLoadingMore
    }
}

class SearchViewModelFactory(
    private val homeRepository: HomeRepository = HomeRepository(NetworkModule.apiService),
    private val projectSearchRepository: ProjectSearchRepository = ProjectSearchRepository(NetworkModule.apiService),
    private val accountRepository: AccountRepository = AccountRepository(
        NetworkModule.apiService,
        NetworkModule.requireSessionStore()
    ),
    private val historyRepository: HistoryRepository =
        HistoryRepository.getInstance(NetworkModule.requireSessionStoreContext())
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(
                homeRepository,
                projectSearchRepository,
                accountRepository,
                historyRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
