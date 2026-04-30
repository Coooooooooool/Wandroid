package com.eric.wandroid.ui.extra

import com.eric.wandroid.common.auth.LOGIN_EXPIRED_CODE
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.ExtraContentMode
import com.eric.wandroid.data.repository.ExtraContentRepository
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

class ExtraContentViewModel(
    private val initialMode: ExtraContentMode,
    private val initialShareUserId: Int?,
    private val initialShareUserName: String,
    private val repository: ExtraContentRepository,
    private val accountRepository: com.eric.wandroid.data.repository.AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExtraContentUiState())
    val uiState: StateFlow<ExtraContentUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _authRequired = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val authRequired: SharedFlow<String> = _authRequired.asSharedFlow()

    init {
        _uiState.update {
            it.copy(
                shareUserId = initialShareUserId,
                shareUserName = initialShareUserName
            )
        }
        requestOverview(mode = initialMode, isRefresh = false)
    }

    fun refresh() {
        if (isBusy()) return
        requestOverview(mode = _uiState.value.mode, isRefresh = true)
    }

    fun retry() {
        if (isBusy()) return
        requestOverview(mode = _uiState.value.mode, isRefresh = false)
    }

    fun selectWechatCategory(category: ProjectCategory) {
        val state = _uiState.value
        if (isBusy() || state.mode != ExtraContentMode.Wechat || state.selectedWechatCategoryId == category.id) return

        _uiState.update {
            it.copy(
                isCategoryLoading = true,
                blockingErrorMessage = null,
                selectedWechatCategoryId = category.id,
                selectedWechatCategoryName = category.name,
                currentPage = ExtraContentRepository.WECHAT_FIRST_PAGE,
                canLoadMore = true,
                articles = emptyList()
            )
        }
        viewModelScope.launch {
            when (val result = repository.loadMore(
                mode = ExtraContentMode.Wechat,
                page = ExtraContentRepository.WECHAT_FIRST_PAGE,
                wechatCategory = category
            )) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isCategoryLoading = false,
                            articles = data.articles,
                            currentPage = data.page,
                            canLoadMore = data.canLoadMore
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

        val nextPage = state.currentPage + 1
        val wechatCategory = if (state.mode == ExtraContentMode.Wechat) {
            state.wechatCategories.firstOrNull { it.id == state.selectedWechatCategoryId } ?: return
        } else {
            null
        }

        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            when (
                val result = repository.loadMore(
                    mode = state.mode,
                    page = nextPage,
                    wechatCategory = wechatCategory,
                    shareUserId = state.shareUserId
                )
            ) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            articles = it.articles + data.articles,
                            currentPage = data.page,
                            canLoadMore = data.canLoadMore
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

        _uiState.update { it.copy(collectingArticleIds = it.collectingArticleIds + article.id) }
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
                            articles = current.articles.map { listed ->
                                if (listed.id == article.id) {
                                    listed.copy(isCollected = !article.isCollected)
                                } else {
                                    listed
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

    fun submitShare(title: String, link: String) {
        val state = _uiState.value
        if (state.isSubmittingShare) return
        val trimmedTitle = title.trim()
        val trimmedLink = link.trim()
        if (trimmedTitle.isBlank() || trimmedLink.isBlank()) {
            _messages.tryEmit("Title and link are required.")
            return
        }

        _uiState.update { it.copy(isSubmittingShare = true) }
        viewModelScope.launch {
            when (val result = repository.addSharedArticle(trimmedTitle, trimmedLink)) {
                is AppResult.Success -> {
                    _messages.tryEmit("Shared successfully.")
                    _uiState.update { it.copy(isSubmittingShare = false) }
                    requestOverview(mode = ExtraContentMode.PrivateShare, isRefresh = false)
                }

                is AppResult.Error -> {
                    _uiState.update { it.copy(isSubmittingShare = false) }
                    emitAuthRequiredIfNeeded(result)
                    _messages.tryEmit(result.message)
                }
            }
        }
    }

    fun deleteSharedArticle(article: Article) {
        val state = _uiState.value
        if (state.collectingArticleIds.contains(article.id)) return

        _uiState.update { it.copy(collectingArticleIds = it.collectingArticleIds + article.id) }
        viewModelScope.launch {
            when (val result = repository.deletePrivateSharedArticle(article.id)) {
                is AppResult.Success -> {
                    _uiState.update { current ->
                        current.copy(
                            articles = current.articles.filterNot { it.id == article.id },
                            collectingArticleIds = current.collectingArticleIds - article.id
                        )
                    }
                    _messages.tryEmit("Deleted successfully.")
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

    private fun requestOverview(mode: ExtraContentMode, isRefresh: Boolean) {
        _uiState.update {
            it.copy(
                mode = mode,
                isInitialLoading = !isRefresh,
                isRefreshing = isRefresh,
                isCategoryLoading = false,
                isLoadingMore = false,
                blockingErrorMessage = null,
                wechatCategories = if (mode == ExtraContentMode.Wechat) it.wechatCategories else emptyList(),
                selectedWechatCategoryId = if (mode == ExtraContentMode.Wechat) it.selectedWechatCategoryId else null,
                selectedWechatCategoryName = if (mode == ExtraContentMode.Wechat) it.selectedWechatCategoryName else "",
                shareUserName = if (mode == ExtraContentMode.ShareUser) it.shareUserName else "",
                articles = emptyList(),
                currentPage = when (mode) {
                    ExtraContentMode.Wenda -> ExtraContentRepository.WENDA_FIRST_PAGE
                    ExtraContentMode.Square -> ExtraContentRepository.SQUARE_FIRST_PAGE
                    ExtraContentMode.Wechat -> ExtraContentRepository.WECHAT_FIRST_PAGE
                    ExtraContentMode.LatestProject -> ExtraContentRepository.LATEST_PROJECT_FIRST_PAGE
                    ExtraContentMode.ShareUser -> ExtraContentRepository.SHARE_USER_FIRST_PAGE
                    ExtraContentMode.PrivateShare -> ExtraContentRepository.PRIVATE_SHARE_FIRST_PAGE
                }
            )
        }

        viewModelScope.launch {
            when (
                val result = repository.loadOverview(
                    mode = mode,
                    selectedWechatCategoryId = _uiState.value.selectedWechatCategoryId,
                    shareUserId = _uiState.value.shareUserId
                )
            ) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            blockingErrorMessage = null,
                            wechatCategories = data.wechatCategories,
                            selectedWechatCategoryId = data.selectedWechatCategory?.id,
                            selectedWechatCategoryName = data.selectedWechatCategory?.name.orEmpty(),
                            shareUserName = data.authorName,
                            articles = data.articles,
                            currentPage = data.page,
                            canLoadMore = data.canLoadMore
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            blockingErrorMessage = result.message,
                            wechatCategories = emptyList(),
                            selectedWechatCategoryId = null,
                            selectedWechatCategoryName = "",
                            shareUserName = "",
                            articles = emptyList()
                        )
                    }
                    emitAuthRequiredIfNeeded(result)
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
        return state.isInitialLoading || state.isRefreshing || state.isCategoryLoading || state.isLoadingMore
    }
}

class ExtraContentViewModelFactory(
    private val initialMode: ExtraContentMode,
    private val initialShareUserId: Int? = null,
    private val initialShareUserName: String = "",
    private val repository: ExtraContentRepository = ExtraContentRepository(NetworkModule.apiService),
    private val accountRepository: com.eric.wandroid.data.repository.AccountRepository = com.eric.wandroid.data.repository.AccountRepository(
        NetworkModule.apiService,
        NetworkModule.requireSessionStore()
    )
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExtraContentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExtraContentViewModel(
                initialMode,
                initialShareUserId,
                initialShareUserName,
                repository,
                accountRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
