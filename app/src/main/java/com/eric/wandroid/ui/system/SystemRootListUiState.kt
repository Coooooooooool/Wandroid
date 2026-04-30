package com.eric.wandroid.ui.system

import com.eric.wandroid.domain.model.SystemRootCategory

data class SystemRootListUiState(
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val blockingErrorMessage: String? = null,
    val roots: List<SystemRootCategory> = emptyList()
)
