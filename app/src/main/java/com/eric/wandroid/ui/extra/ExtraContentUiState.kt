package com.eric.wandroid.ui.extra

import com.eric.wandroid.data.repository.ExtraContentMode
import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.ProjectCategory

data class ExtraContentUiState(
    val mode: ExtraContentMode = ExtraContentMode.Wenda,
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isCategoryLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val blockingErrorMessage: String? = null,
    val wechatCategories: List<ProjectCategory> = emptyList(),
    val selectedWechatCategoryId: Int? = null,
    val selectedWechatCategoryName: String = "",
    val shareUserId: Int? = null,
    val shareUserName: String = "",
    val articles: List<Article> = emptyList(),
    val currentPage: Int = 0,
    val canLoadMore: Boolean = true,
    val collectingArticleIds: Set<Int> = emptySet(),
    val isSubmittingShare: Boolean = false
) {
    val hasContent: Boolean
        get() = articles.isNotEmpty() || wechatCategories.isNotEmpty()
}
