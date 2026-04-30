package com.eric.wandroid.ui.search

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.HotKey
import com.eric.wandroid.domain.model.SearchHistoryItem

data class SearchUiState(
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val blockingErrorMessage: String? = null,
    val hotKeys: List<HotKey> = emptyList(),
    val searchHistory: List<SearchHistoryItem> = emptyList(),
    val keyword: String = "",
    val currentPage: Int = 0,
    val canLoadMore: Boolean = true,
    val articles: List<Article> = emptyList(),
    val collectingArticleIds: Set<Int> = emptySet()
) {
    val isResultMode: Boolean
        get() = keyword.isNotBlank()

    val hasContent: Boolean
        get() = hotKeys.isNotEmpty() || searchHistory.isNotEmpty() || articles.isNotEmpty()
}
