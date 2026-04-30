package com.eric.wandroid.ui.moyu

data class MoyuVideoUiState(
    val isInitialLoading: Boolean = false,
    val blockingErrorMessage: String? = null,
    val videoUrls: List<String> = emptyList(),
    val activePosition: Int = 0,
    val isLoadingMore: Boolean = false
) {
    val hasContent: Boolean
        get() = videoUrls.isNotEmpty()
}
