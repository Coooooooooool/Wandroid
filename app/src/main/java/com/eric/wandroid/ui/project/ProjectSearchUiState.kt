package com.eric.wandroid.ui.project

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.ProjectCategory

enum class ProjectSearchMode {
    Project,
    Search
}

data class ProjectSearchUiState(
    val mode: ProjectSearchMode = ProjectSearchMode.Project,
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isCategoryLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val blockingErrorMessage: String? = null,
    val categories: List<ProjectCategory> = emptyList(),
    val selectedCategoryId: Int? = null,
    val selectedCategoryName: String = "",
    val searchKeyword: String = "",
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val articles: List<Article> = emptyList(),
    val collectingArticleIds: Set<Int> = emptySet()
) {
    val hasContent: Boolean
        get() = categories.isNotEmpty() || articles.isNotEmpty()

    val isSearchMode: Boolean
        get() = mode == ProjectSearchMode.Search
}
