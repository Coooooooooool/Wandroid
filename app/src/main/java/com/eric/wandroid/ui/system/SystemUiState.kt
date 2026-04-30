package com.eric.wandroid.ui.system

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.NavigationGroup
import com.eric.wandroid.domain.model.SystemRootCategory

data class SystemUiState(
    val mode: SystemContentMode = SystemContentMode.System,
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isCategoryLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val blockingErrorMessage: String? = null,
    val roots: List<SystemRootCategory> = emptyList(),
    val navigationGroups: List<NavigationGroup> = emptyList(),
    val selectedCategoryId: Int? = null,
    val selectedCategoryName: String = "",
    val currentPage: Int = 0,
    val canLoadMore: Boolean = true,
    val articles: List<Article> = emptyList(),
    val collectingArticleIds: Set<Int> = emptySet()
) {
    val hasOverview: Boolean
        get() = when (mode) {
            SystemContentMode.System -> roots.isNotEmpty()
            SystemContentMode.Navigation -> navigationGroups.isNotEmpty()
        }
}
