package com.eric.wandroid.ui.system

import com.eric.wandroid.common.auth.LOGIN_EXPIRED_CODE
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.AccountRepository
import com.eric.wandroid.data.repository.SystemRepository
import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.SystemChildCategory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SystemViewModel(
    private val initialMode: SystemContentMode,
    private val repository: SystemRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SystemUiState(mode = initialMode))
    val uiState: StateFlow<SystemUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _authRequired = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val authRequired: SharedFlow<String> = _authRequired.asSharedFlow()

    init {
        requestContent(isRefresh = false)
    }

    fun refresh() {
        if (isBusy()) return
        requestContent(isRefresh = true)
    }

    fun retry() {
        if (isBusy()) return
        requestContent(isRefresh = false)
    }

    fun selectCategory(category: SystemChildCategory) {
        val state = _uiState.value
        if (state.mode != SystemContentMode.System || isBusy() || state.selectedCategoryId == category.id) return

        _uiState.update {
            it.copy(
                isCategoryLoading = true,
                blockingErrorMessage = null,
                selectedCategoryId = category.id,
                selectedCategoryName = category.name,
                currentPage = 0,
                canLoadMore = true,
                articles = emptyList()
            )
        }

        viewModelScope.launch {
            when (val result = repository.loadCategoryArticles(category, page = 0)) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isCategoryLoading = false,
                            currentPage = data.page,
                            canLoadMore = data.canLoadMore,
                            articles = data.articles
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isCategoryLoading = false,
                            blockingErrorMessage = if (it.hasOverview) null else result.message
                        )
                    }
                    _messages.tryEmit(result.message)
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.mode != SystemContentMode.System) return

        val selectedCategory = findSelectedCategory() ?: return
        if (isBusy() || !state.canLoadMore) return

        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            when (val result = repository.loadCategoryArticles(selectedCategory, state.currentPage + 1)) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            currentPage = data.page,
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
        if (state.mode != SystemContentMode.System || state.collectingArticleIds.contains(article.id)) return

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

    private fun requestContent(isRefresh: Boolean) {
        val selectedId = _uiState.value.selectedCategoryId
        _uiState.update {
            it.copy(
                isInitialLoading = !isRefresh,
                isRefreshing = isRefresh,
                isCategoryLoading = false,
                isLoadingMore = false,
                blockingErrorMessage = null
            )
        }

        viewModelScope.launch {
            when (_uiState.value.mode) {
                SystemContentMode.System -> {
                    when (val result = repository.loadSystemOverview(selectedId)) {
                        is AppResult.Success -> {
                            val data = result.data
                            _uiState.update {
                                it.copy(
                                    isInitialLoading = false,
                                    isRefreshing = false,
                                    blockingErrorMessage = null,
                                    roots = data.roots,
                                    navigationGroups = emptyList(),
                                    selectedCategoryId = data.selectedCategory?.id,
                                    selectedCategoryName = data.selectedCategory?.name.orEmpty(),
                                    currentPage = data.page,
                                    canLoadMore = data.canLoadMore,
                                    articles = data.articles
                                )
                            }
                        }

                        is AppResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isInitialLoading = false,
                                    isRefreshing = false,
                                    blockingErrorMessage = result.message
                                )
                            }
                        }
                    }
                }

                SystemContentMode.Navigation -> {
                    when (val result = repository.loadNavigationGroups()) {
                        is AppResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isInitialLoading = false,
                                    isRefreshing = false,
                                    roots = emptyList(),
                                    selectedCategoryId = null,
                                    selectedCategoryName = "",
                                    currentPage = 0,
                                    canLoadMore = false,
                                    articles = emptyList(),
                                    navigationGroups = result.data
                                )
                            }
                        }

                        is AppResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isInitialLoading = false,
                                    isRefreshing = false,
                                    blockingErrorMessage = result.message
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun findSelectedCategory(): SystemChildCategory? {
        val selectedId = _uiState.value.selectedCategoryId ?: return null
        return _uiState.value.roots.flatMap { it.children }.firstOrNull { it.id == selectedId }
    }

    private fun emitAuthRequiredIfNeeded(error: AppResult.Error) {
        if (error.code == LOGIN_EXPIRED_CODE) {
            _authRequired.tryEmit(error.message)
        }
    }

    private fun isBusy(): Boolean {
        val state = _uiState.value
        return state.isInitialLoading || state.isRefreshing || state.isCategoryLoading || state.isLoadingMore
    }
}

class SystemViewModelFactory(
    private val initialMode: SystemContentMode,
    private val repository: SystemRepository = SystemRepository(NetworkModule.apiService),
    private val accountRepository: AccountRepository = AccountRepository(
        NetworkModule.apiService,
        NetworkModule.requireSessionStore()
    )
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SystemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SystemViewModel(initialMode, repository, accountRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
