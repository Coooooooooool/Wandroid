package com.eric.wandroid.ui.home

import com.eric.wandroid.common.auth.LOGIN_EXPIRED_CODE
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.AccountRepository
import com.eric.wandroid.data.repository.HomeRepository
import com.eric.wandroid.domain.model.Article
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _authRequired = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val authRequired: SharedFlow<String> = _authRequired.asSharedFlow()

    init {
        requestPage(page = 0, mode = LoadMode.Initial)
    }

    fun loadInitial() {
        if (_uiState.value.isInitialLoading || _uiState.value.hasContent) {
            return
        }
        requestPage(page = 0, mode = LoadMode.Initial)
    }

    fun refresh() {
        if (isBusy()) {
            return
        }
        requestPage(page = 0, mode = LoadMode.Refresh)
    }

    fun retry() {
        if (isBusy()) {
            return
        }
        requestPage(page = 0, mode = LoadMode.Initial)
    }

    fun loadMore() {
        val state = _uiState.value
        if (isBusy() || !state.hasContent || !state.canLoadMore) {
            return
        }
        requestPage(page = state.currentPage + 1, mode = LoadMode.LoadMore)
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
                        if (article.isCollected) {
                            "Removed from favorites."
                        } else {
                            "Added to favorites."
                        }
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

    private fun requestPage(page: Int, mode: LoadMode) {
        val hadContent = _uiState.value.hasContent
        when (mode) {
            LoadMode.Initial -> _uiState.update {
                it.copy(
                    isInitialLoading = true,
                    blockingErrorMessage = null
                )
            }
            LoadMode.Refresh -> _uiState.update {
                it.copy(
                    isRefreshing = true,
                    blockingErrorMessage = null
                )
            }
            LoadMode.LoadMore -> _uiState.update { it.copy(isLoadingMore = true) }
        }

        viewModelScope.launch {
            when (val result = repository.loadHomePage(page)) {
                is AppResult.Success -> handleSuccess(result, mode)
                is AppResult.Error -> handleFailure(result.message, mode, hadContent)
            }
        }
    }

    private fun handleSuccess(result: AppResult.Success<com.eric.wandroid.domain.model.HomePageData>, mode: LoadMode) {
        val data = result.data
        _uiState.update { current ->
            if (mode == LoadMode.LoadMore) {
                    current.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        blockingErrorMessage = null,
                    currentPage = data.page,
                    canLoadMore = data.canLoadMore,
                    articles = current.articles + data.articles
                )
            } else {
                    current.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        blockingErrorMessage = null,
                        currentPage = data.page,
                        canLoadMore = data.canLoadMore,
                        banners = data.banners,
                        hotKeys = data.hotKeys,
                        websites = data.websites,
                        popularRoutes = data.popularRoutes,
                        popularWenda = data.popularWenda,
                        popularColumns = data.popularColumns,
                        articles = data.articles
                    )
            }
        }
    }

    private fun handleFailure(message: String, mode: LoadMode, hadContent: Boolean) {
        if (hadContent) {
            _uiState.update {
                it.copy(
                    isInitialLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false
                )
            }
            _messages.tryEmit(message)
            return
        }

        _uiState.update {
            it.copy(
                isInitialLoading = false,
                isRefreshing = false,
                isLoadingMore = false,
                blockingErrorMessage = message
            )
        }
        if (mode == LoadMode.LoadMore) {
            _messages.tryEmit(message)
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

class HomeViewModelFactory(
    private val repository: HomeRepository = HomeRepository(NetworkModule.apiService),
    private val accountRepository: AccountRepository = AccountRepository(
        NetworkModule.apiService,
        NetworkModule.requireSessionStore()
    )
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, accountRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private enum class LoadMode {
    Initial,
    Refresh,
    LoadMore
}
