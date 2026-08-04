package com.eric.wandroid.ui.moyu.wallpaper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.MoeHuWallpaperRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WallpaperViewModel(
    private val repository: MoeHuWallpaperRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        WallpaperUiState(
            selectedGroupIndex = 1,
            selectedCategory = WallpaperCategoryCatalog.groups[1].categories.first()
        )
    )
    val uiState: StateFlow<WallpaperUiState> = _uiState.asStateFlow()

    private var requestJob: Job? = null

    init {
        loadSelectedCategory(isRefresh = false)
    }

    fun selectGroup(groupIndex: Int) {
        if (groupIndex !in WallpaperCategoryCatalog.groups.indices) return
        val category = WallpaperCategoryCatalog.groups[groupIndex].categories.firstOrNull() ?: return
        if (_uiState.value.selectedGroupIndex == groupIndex && _uiState.value.selectedCategory == category) return

        _uiState.update {
            it.copy(
                selectedGroupIndex = groupIndex,
                selectedCategory = category
            )
        }
        loadSelectedCategory(isRefresh = false)
    }

    fun selectCategory(category: WallpaperCategory) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
        loadSelectedCategory(isRefresh = false)
    }

    fun refresh() {
        if (_uiState.value.isInitialLoading || _uiState.value.isRefreshing) return
        loadSelectedCategory(isRefresh = true)
    }

    fun retry() {
        if (_uiState.value.isInitialLoading) return
        loadSelectedCategory(isRefresh = false)
    }

    private fun loadSelectedCategory(isRefresh: Boolean) {
        requestJob?.cancel()
        val category = _uiState.value.selectedCategory
        _uiState.update {
            if (isRefresh && it.wallpapers.isNotEmpty()) {
                it.copy(isRefreshing = true, errorMessage = null)
            } else {
                it.copy(
                    wallpapers = emptyList(),
                    isInitialLoading = true,
                    isRefreshing = false,
                    errorMessage = null
                )
            }
        }

        requestJob = viewModelScope.launch {
            when (val result = repository.loadWallpapers(category.id)) {
                is AppResult.Success -> {
                    if (_uiState.value.selectedCategory != category) return@launch
                    _uiState.update {
                        it.copy(
                            wallpapers = result.data,
                            isInitialLoading = false,
                            isRefreshing = false,
                            errorMessage = null
                        )
                    }
                }

                is AppResult.Error -> {
                    if (_uiState.value.selectedCategory != category) return@launch
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
}

class WallpaperViewModelFactory(
    private val repository: MoeHuWallpaperRepository = MoeHuWallpaperRepository(NetworkModule.moeHuApiService)
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WallpaperViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WallpaperViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
