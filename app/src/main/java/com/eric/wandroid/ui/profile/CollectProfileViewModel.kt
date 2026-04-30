package com.eric.wandroid.ui.profile

import com.eric.wandroid.common.auth.LOGIN_EXPIRED_CODE
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.AccountRepository
import com.eric.wandroid.domain.model.Article
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CollectProfileViewModel(
    private val repository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CollectProfileUiState())
    val uiState: StateFlow<CollectProfileUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _authRequired = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val authRequired: SharedFlow<String> = _authRequired.asSharedFlow()

    init {
        requestOverview(isRefresh = false)
    }

    fun refresh() {
        if (isBusy()) return
        requestOverview(isRefresh = true)
    }

    fun retry() {
        if (isBusy()) return
        requestOverview(isRefresh = false)
    }

    fun loadMore() {
        val state = _uiState.value
        if (isBusy() || !state.canLoadMore || state.articles.isEmpty()) return

        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            when (val result = repository.loadCollectedArticles(state.currentPage + 1)) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            currentPage = data.page,
                            canLoadMore = data.canLoadMore,
                            articles = it.articles + data.articles.map { article -> article.copy(isCollected = true) }
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
            when (val result = repository.uncollectArticle(article.id)) {
                is AppResult.Success -> {
                    _uiState.update { current ->
                        current.copy(
                            articles = current.articles.filterNot { it.id == article.id },
                            collectingArticleIds = current.collectingArticleIds - article.id
                        )
                    }
                    _messages.tryEmit("Removed from favorites.")
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

    private fun requestOverview(isRefresh: Boolean) {
        _uiState.update {
            it.copy(
                isInitialLoading = !isRefresh,
                isRefreshing = isRefresh,
                isLoadingMore = false,
                blockingErrorMessage = null
            )
        }

        viewModelScope.launch {
            val favoritesResult = repository.loadCollectedArticles(page = 0)

            val favorites = (favoritesResult as? AppResult.Success)?.data
            val error = listOf(favoritesResult)
                .filterIsInstance<AppResult.Error>()
                .firstOrNull()

            if (favorites != null) {
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        blockingErrorMessage = null,
                        articles = favorites?.articles.orEmpty().map { article -> article.copy(isCollected = true) },
                        currentPage = favorites?.page ?: 0,
                        canLoadMore = favorites?.canLoadMore ?: false
                    )
                }
                error?.let(::emitAuthRequiredIfNeeded)
                error?.let { appError -> _messages.tryEmit(appError.message) }
            } else {
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        blockingErrorMessage = error?.message ?: "Request failed. Please try again later."
                    )
                }
                error?.let(::emitAuthRequiredIfNeeded)
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

class CollectProfileViewModelFactory(
    private val repository: AccountRepository = AccountRepository(
        NetworkModule.apiService,
        NetworkModule.requireSessionStore()
    )
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CollectProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CollectProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
