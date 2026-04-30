package com.eric.wandroid.ui.system

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.SystemChildCategory

sealed interface SystemDetailListItem {
    data class CategorySelector(
        val categories: List<SystemChildCategory>,
        val selectedCategoryId: Int?
    ) : SystemDetailListItem

    data class ArticleHeader(
        val categoryName: String,
        val count: Int
    ) : SystemDetailListItem

    data class ArticleRow(val article: Article) : SystemDetailListItem

    data class LoadMoreFooter(
        val isLoading: Boolean,
        val canLoadMore: Boolean
    ) : SystemDetailListItem
}
