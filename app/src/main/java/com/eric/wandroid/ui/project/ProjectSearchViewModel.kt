package com.eric.wandroid.ui.project

import com.eric.wandroid.common.auth.LOGIN_EXPIRED_CODE
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.AccountRepository
import com.eric.wandroid.data.repository.ProjectSearchRepository
import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.ProjectCategory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectSearchViewModel(
    private val repository: ProjectSearchRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProjectSearchUiState())
    val uiState: StateFlow<ProjectSearchUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _authRequired = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val authRequired: SharedFlow<String> = _authRequired.asSharedFlow()

    init {
        requestProjectOverview(isRefresh = false)
    }

    fun refresh() {
        if (isBusy()) return
        val state = _uiState.value
        if (state.isSearchMode && state.searchKeyword.isNotBlank()) {
            requestSearch(state.searchKeyword, isRefresh = true)
        } else {
            requestProjectOverview(isRefresh = true)
        }
    }

    fun retry() {
        if (isBusy()) return
        val state = _uiState.value
        if (state.isSearchMode && state.searchKeyword.isNotBlank()) {
            requestSearch(state.searchKeyword, isRefresh = false)
        } else {
            requestProjectOverview(isRefresh = false)
        }
    }

    fun submitSearch(keyword: String) {
        val normalizedKeyword = keyword.trim()
        if (isBusy() || normalizedKeyword.isBlank()) return
        requestSearch(normalizedKeyword, isRefresh = false)
    }

    fun clearSearch() {
        if (isBusy()) return
        requestProjectOverview(isRefresh = false, forceProjectMode = true)
    }

    fun selectCategory(category: ProjectCategory) {
        val state = _uiState.value
        if (isBusy() || state.selectedCategoryId == category.id && !state.isSearchMode) return

        _uiState.update {
            it.copy(
                mode = ProjectSearchMode.Project,
                isCategoryLoading = true,
                blockingErrorMessage = null,
                selectedCategoryId = category.id,
                selectedCategoryName = category.name,
                searchKeyword = "",
                currentPage = ProjectSearchRepository.PROJECT_FIRST_PAGE,
                canLoadMore = true,
                articles = emptyList()
            )
        }

        viewModelScope.launch {
            when (val result = repository.loadProjectArticles(category, ProjectSearchRepository.PROJECT_FIRST_PAGE)) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isCategoryLoading = false,
                            currentPage = data.projectPage,
                            canLoadMore = data.canLoadMore,
                            articles = data.articles
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isCategoryLoading = false) }
                    _messages.tryEmit(result.message)
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (isBusy() || !state.canLoadMore) return

        if (state.isSearchMode) {
            val keyword = state.searchKeyword
            if (keyword.isBlank()) return
            loadMoreSearch(keyword, state.currentPage + 1)
        } else {
            val category = findSelectedCategory() ?: return
            loadMoreProject(category, state.currentPage + 1)
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

    private fun requestProjectOverview(
        isRefresh: Boolean,
        forceProjectMode: Boolean = false
    ) {
        val selectedId = if (forceProjectMode) _uiState.value.selectedCategoryId else _uiState.value.selectedCategoryId
        _uiState.update {
            it.copy(
                mode = ProjectSearchMode.Project,
                isInitialLoading = !isRefresh,
                isRefreshing = isRefresh,
                isCategoryLoading = false,
                isLoadingMore = false,
                blockingErrorMessage = null,
                searchKeyword = ""
            )
        }

        viewModelScope.launch {
            when (val result = repository.loadProjectOverview(selectedId)) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            blockingErrorMessage = null,
                            categories = data.categories,
                            selectedCategoryId = data.selectedCategory?.id,
                            selectedCategoryName = data.selectedCategory?.name.orEmpty(),
                            currentPage = data.projectPage,
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
    }

    private fun requestSearch(keyword: String, isRefresh: Boolean) {
        _uiState.update {
            it.copy(
                mode = ProjectSearchMode.Search,
                isInitialLoading = !isRefresh && it.articles.isEmpty(),
                isRefreshing = isRefresh,
                isCategoryLoading = false,
                isLoadingMore = false,
                blockingErrorMessage = null,
                searchKeyword = keyword,
                currentPage = ProjectSearchRepository.SEARCH_FIRST_PAGE,
                canLoadMore = true,
                articles = if (isRefresh) it.articles else emptyList()
            )
        }

        viewModelScope.launch {
            when (val result = repository.searchArticles(keyword, ProjectSearchRepository.SEARCH_FIRST_PAGE)) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            blockingErrorMessage = null,
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
                            isRefreshing = false,
                            blockingErrorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun loadMoreProject(category: ProjectCategory, page: Int) {
        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            when (val result = repository.loadProjectArticles(category, page)) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            currentPage = data.projectPage,
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

    private fun loadMoreSearch(keyword: String, page: Int) {
        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            when (val result = repository.searchArticles(keyword, page)) {
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

    private fun findSelectedCategory(): ProjectCategory? {
        val selectedId = _uiState.value.selectedCategoryId ?: return null
        return _uiState.value.categories.firstOrNull { it.id == selectedId }
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

class ProjectSearchViewModelFactory(
    private val repository: ProjectSearchRepository = ProjectSearchRepository(NetworkModule.apiService),
    private val accountRepository: AccountRepository = AccountRepository(
        NetworkModule.apiService,
        NetworkModule.requireSessionStore()
    )
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectSearchViewModel(repository, accountRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
