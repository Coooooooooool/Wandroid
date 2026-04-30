package com.eric.wandroid.ui.extra

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.ProjectCategory

sealed interface ExtraContentListItem {
    data class WechatCategorySection(
        val categories: List<ProjectCategory>,
        val selectedCategoryId: Int?
    ) : ExtraContentListItem
    data class ArticleHeader(val title: String, val count: Int) : ExtraContentListItem
    data class ArticleRow(val article: Article) : ExtraContentListItem
    data class LoadMoreFooter(val isLoading: Boolean, val canLoadMore: Boolean) : ExtraContentListItem
}
