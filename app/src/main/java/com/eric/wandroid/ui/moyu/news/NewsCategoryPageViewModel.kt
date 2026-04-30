package com.eric.wandroid.ui.moyu.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewsCategoryPageViewModel(
    private val categoryType: String,
    private val repository: NewsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewsCategoryPageUiState(categoryType = categoryType))
    val uiState: StateFlow<NewsCategoryPageUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private var nextPage = 1

    init {
        loadInitial()
    }

    fun refresh() {
        if (_uiState.value.isRefreshing || _uiState.value.isInitialLoading) return
        _uiState.update {
            it.copy(
                isRefreshing = true,
                blockingErrorMessage = null
            )
        }
        loadPage(page = 1, replace = true, isRefresh = true)
    }

    fun retry() {
        if (_uiState.value.isInitialLoading) return
        loadInitial()
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isInitialLoading || state.isRefreshing || state.isLoadingMore || !state.canLoadMore) return
        _uiState.update { it.copy(isLoadingMore = true) }
        loadPage(page = nextPage, replace = false, isRefresh = false)
    }

    private fun loadInitial() {
        _uiState.update {
            it.copy(
                articles = emptyList(),
                isInitialLoading = true,
                isRefreshing = false,
                isLoadingMore = false,
                canLoadMore = true,
                blockingErrorMessage = null
            )
        }
        nextPage = 1
        loadPage(page = 1, replace = true, isRefresh = false)
    }

    private fun loadPage(page: Int, replace: Boolean, isRefresh: Boolean) {
        viewModelScope.launch {
            when (val result = repository.loadNewsList(type = categoryType, page = page)) {
                is AppResult.Success -> {
                    val pageData = result.data
                    _uiState.update { current ->
                        current.copy(
                            articles = if (replace) {
                                pageData.articles
                            } else {
                                current.articles + pageData.articles
                            },
                            isInitialLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            canLoadMore = pageData.hasMore,
                            blockingErrorMessage = null
                        )
                    }
                    nextPage = page + 1
                }

                is AppResult.Error -> {
                    _uiState.update { current ->
                        current.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            blockingErrorMessage = if (replace && current.articles.isEmpty()) {
                                result.message
                            } else {
                                current.blockingErrorMessage
                            }
                        )
                    }
                    if (!replace || !isRefresh) {
                        _messages.tryEmit(result.message)
                    } else if (_uiState.value.articles.isNotEmpty()) {
                        _messages.tryEmit(result.message)
                    }
                }
            }
        }
    }
}

class NewsCategoryPageViewModelFactory(
    private val categoryType: String,
    private val repository: NewsRepository = NewsRepository(NetworkModule.newsApiService)
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsCategoryPageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsCategoryPageViewModel(categoryType, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
