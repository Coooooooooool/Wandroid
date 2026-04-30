package com.eric.wandroid.ui.moyu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.MoyuRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MoyuVideoViewModel(
    private val repository: MoyuRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MoyuVideoUiState(isInitialLoading = true))
    val uiState: StateFlow<MoyuVideoUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        loadInitialVideos()
    }

    fun retry() {
        if (_uiState.value.isInitialLoading) return
        loadInitialVideos()
    }

    fun updateActivePosition(position: Int) {
        _uiState.update { it.copy(activePosition = position) }
        maybeLoadMore(position)
    }

    private fun loadInitialVideos() {
        _uiState.update {
            it.copy(
                isInitialLoading = true,
                blockingErrorMessage = null,
                videoUrls = emptyList(),
                activePosition = 0,
                isLoadingMore = false
            )
        }
        viewModelScope.launch {
            val collected = mutableListOf<String>()
            repeat(INITIAL_VIDEO_COUNT) {
                when (val result = repository.loadRandomVideo()) {
                    is AppResult.Success -> collected += result.data
                    is AppResult.Error -> {
                        if (collected.isEmpty()) {
                            _uiState.update {
                                it.copy(
                                    isInitialLoading = false,
                                    blockingErrorMessage = result.message
                                )
                            }
                            return@launch
                        }
                    }
                }
            }
            _uiState.update {
                it.copy(
                    isInitialLoading = false,
                    blockingErrorMessage = null,
                    videoUrls = collected.distinct()
                )
            }
        }
    }

    private fun maybeLoadMore(position: Int) {
        val state = _uiState.value
        if (state.isInitialLoading || state.isLoadingMore) return
        if (state.videoUrls.size - position > LOAD_MORE_THRESHOLD) return

        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            when (val result = repository.loadRandomVideo()) {
                is AppResult.Success -> {
                    _uiState.update { current ->
                        current.copy(
                            isLoadingMore = false,
                            videoUrls = current.videoUrls + result.data
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoadingMore = false) }
                    _messages.tryEmit(result.message)
                }
            }
        }
    }

    companion object {
        private const val INITIAL_VIDEO_COUNT = 3
        private const val LOAD_MORE_THRESHOLD = 2
    }
}

class MoyuVideoViewModelFactory(
    private val repository: MoyuRepository = MoyuRepository(NetworkModule.moyuApiService)
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoyuVideoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoyuVideoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
