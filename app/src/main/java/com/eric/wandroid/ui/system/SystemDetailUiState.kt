package com.eric.wandroid.ui.system

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.SystemChildCategory

data class SystemDetailUiState(
    val rootName: String = "",
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isCategoryLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val blockingErrorMessage: String? = null,
    val children: List<SystemChildCategory> = emptyList(),
    val selectedCategoryId: Int? = null,
    val selectedCategoryName: String = "",
    val currentPage: Int = 0,
    val canLoadMore: Boolean = true,
    val articles: List<Article> = emptyList(),
    val collectingArticleIds: Set<Int> = emptySet()
)
