package com.eric.wandroid.ui.moyu.internet

import com.eric.wandroid.domain.model.InternetNewsArticle

data class InternetNewsUiState(
    val keyword: String = DEFAULT_INTERNET_NEWS_KEYWORD,
    val articles: List<InternetNewsArticle> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val blockingErrorMessage: String? = null
) {
    val hasContent: Boolean
        get() = articles.isNotEmpty()
}

const val DEFAULT_INTERNET_NEWS_KEYWORD = "互联网"
