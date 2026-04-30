package com.eric.wandroid.ui.coin

import com.eric.wandroid.common.auth.LOGIN_EXPIRED_CODE
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PointsViewModel(
    private val repository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PointsUiState())
    val uiState: StateFlow<PointsUiState> = _uiState.asStateFlow()

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
        if (isBusy() || !state.canLoadMore) return

        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            when (val result = repository.loadCoinRecords(state.currentPage + 1)) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            currentPage = data.page,
                            canLoadMore = data.canLoadMore,
                            records = it.records + data.records
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
            val overviewResult = repository.loadCoinOverview()
            val recordsResult = repository.loadCoinRecords(page = 1)

            val overview = (overviewResult as? AppResult.Success)?.data
            val records = (recordsResult as? AppResult.Success)?.data
            val error = listOf(overviewResult, recordsResult)
                .filterIsInstance<AppResult.Error>()
                .firstOrNull()

            if (overview != null || records != null) {
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        blockingErrorMessage = null,
                        overview = overview,
                        records = records?.records.orEmpty(),
                        currentPage = records?.page ?: 1,
                        canLoadMore = records?.canLoadMore ?: false
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

class PointsViewModelFactory(
    private val repository: AccountRepository = AccountRepository(
        NetworkModule.apiService,
        NetworkModule.requireSessionStore()
    )
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PointsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PointsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
