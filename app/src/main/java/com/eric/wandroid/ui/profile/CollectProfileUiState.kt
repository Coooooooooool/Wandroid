package com.eric.wandroid.ui.profile

import com.eric.wandroid.domain.model.Article

data class CollectProfileUiState(
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val blockingErrorMessage: String? = null,
    val articles: List<Article> = emptyList(),
    val currentPage: Int = 0,
    val canLoadMore: Boolean = true,
    val collectingArticleIds: Set<Int> = emptySet()
) {
    val hasContent: Boolean
        get() = articles.isNotEmpty()
}
