package com.eric.wandroid.ui.moyu.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.NewsRepository
import com.eric.wandroid.domain.model.NewsArticle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewsDetailViewModel(
    private val article: NewsArticle,
    private val repository: NewsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewsDetailUiState(article = article))
    val uiState: StateFlow<NewsDetailUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        loadDetail(initial = true)
    }

    fun refresh() {
        if (_uiState.value.isInitialLoading || _uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true, blockingErrorMessage = null) }
        loadDetail(initial = false)
    }

    fun retry() {
        if (_uiState.value.isInitialLoading) return
        _uiState.update { it.copy(isInitialLoading = true, blockingErrorMessage = null) }
        loadDetail(initial = true)
    }

    private fun loadDetail(initial: Boolean) {
        viewModelScope.launch {
            when (val result = repository.loadNewsDetail(article)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            detail = result.data,
                            isInitialLoading = false,
                            isRefreshing = false,
                            blockingErrorMessage = null
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            blockingErrorMessage = if (it.detail == null) result.message else null
                        )
                    }
                    if (!initial || _uiState.value.detail != null) {
                        _messages.tryEmit(result.message)
                    }
                }
            }
        }
    }
}

class NewsDetailViewModelFactory(
    private val article: NewsArticle,
    private val repository: NewsRepository = NewsRepository(NetworkModule.newsApiService)
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsDetailViewModel(article, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
