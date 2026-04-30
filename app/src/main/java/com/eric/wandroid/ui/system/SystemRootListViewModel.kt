package com.eric.wandroid.ui.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.SystemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SystemRootListViewModel(
    private val repository: SystemRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SystemRootListUiState())
    val uiState: StateFlow<SystemRootListUiState> = _uiState.asStateFlow()

    init {
        loadRoots(isRefresh = false)
    }

    fun refresh() {
        if (isBusy()) return
        loadRoots(isRefresh = true)
    }

    fun retry() {
        if (isBusy()) return
        loadRoots(isRefresh = false)
    }

    private fun loadRoots(isRefresh: Boolean) {
        _uiState.update {
            it.copy(
                isInitialLoading = !isRefresh,
                isRefreshing = isRefresh,
                blockingErrorMessage = null
            )
        }

        viewModelScope.launch {
            when (val result = repository.loadSystemRoots()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            roots = result.data,
                            blockingErrorMessage = null
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

    private fun isBusy(): Boolean {
        val state = _uiState.value
        return state.isInitialLoading || state.isRefreshing
    }
}

class SystemRootListViewModelFactory(
    private val repository: SystemRepository = SystemRepository(NetworkModule.apiService)
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SystemRootListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SystemRootListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
