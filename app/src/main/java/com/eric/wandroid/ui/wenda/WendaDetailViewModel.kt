package com.eric.wandroid.ui.wenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.auth.LOGIN_EXPIRED_CODE
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.AccountRepository
import com.eric.wandroid.data.repository.WendaRepository
import com.eric.wandroid.domain.model.Article
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WendaDetailViewModel(
    initialArticle: Article,
    private val repository: WendaRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WendaDetailUiState(article = initialArticle))
    val uiState: StateFlow<WendaDetailUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _authRequired = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val authRequired: SharedFlow<String> = _authRequired.asSharedFlow()

    init {
        loadComments(isRefresh = false)
    }

    fun refresh() {
        if (isBusy()) return
        loadComments(isRefresh = true)
    }

    fun retry() {
        if (isBusy()) return
        loadComments(isRefresh = false)
    }

    fun toggleCollect() {
        val state = _uiState.value
        if (state.isCollecting) return

        _uiState.update { it.copy(isCollecting = true) }
        viewModelScope.launch {
            val article = _uiState.value.article
            val result = if (article.isCollected) {
                accountRepository.uncollectArticle(article.id)
            } else {
                accountRepository.collectArticle(article.id)
            }
            when (result) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isCollecting = false,
                            article = it.article.copy(isCollected = !article.isCollected)
                        )
                    }
                    _messages.tryEmit(
                        if (article.isCollected) "Removed from favorites." else "Added to favorites."
                    )
                }

                is AppResult.Error -> {
                    _uiState.update { it.copy(isCollecting = false) }
                    emitAuthRequiredIfNeeded(result)
                    _messages.tryEmit(result.message)
                }
            }
        }
    }

    private fun loadComments(isRefresh: Boolean) {
        _uiState.update {
            it.copy(
                isInitialLoading = !isRefresh,
                isRefreshing = isRefresh,
                commentsErrorMessage = null,
                comments = if (isRefresh) it.comments else emptyList()
            )
        }

        viewModelScope.launch {
            when (val result = repository.loadComments(_uiState.value.article.id)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            comments = result.data,
                            commentsErrorMessage = null
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            comments = emptyList(),
                            commentsErrorMessage = result.message
                        )
                    }
                    _messages.tryEmit(result.message)
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
        return state.isInitialLoading || state.isRefreshing
    }
}

class WendaDetailViewModelFactory(
    private val initialArticle: Article,
    private val repository: WendaRepository = WendaRepository(NetworkModule.apiService),
    private val accountRepository: AccountRepository = AccountRepository(
        NetworkModule.apiService,
        NetworkModule.requireSessionStore()
    )
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WendaDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WendaDetailViewModel(initialArticle, repository, accountRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
