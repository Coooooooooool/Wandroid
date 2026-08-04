package com.eric.wandroid.ui.home

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.Banner
import com.eric.wandroid.domain.model.HotKey
import com.eric.wandroid.domain.model.PopularColumn
import com.eric.wandroid.domain.model.PopularRoute
import com.eric.wandroid.domain.model.Website

data class HomeUiState(
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val blockingErrorMessage: String? = null,
    val currentPage: Int = 0,
    val canLoadMore: Boolean = true,
    val banners: List<Banner> = emptyList(),
    val hotKeys: List<HotKey> = emptyList(),
    val websites: List<Website> = emptyList(),
    val popularRoutes: List<PopularRoute> = emptyList(),
    val popularWenda: List<Article> = emptyList(),
    val popularColumns: List<PopularColumn> = emptyList(),
    val articles: List<Article> = emptyList(),
    val collectingArticleIds: Set<Int> = emptySet()
) {
    val hasContent: Boolean
        get() = banners.isNotEmpty() || popularRoutes.isNotEmpty() || popularWenda.isNotEmpty() || popularColumns.isNotEmpty() || articles.isNotEmpty()
}
