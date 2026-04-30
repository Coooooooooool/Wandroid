package com.eric.wandroid.ui.project

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.ProjectCategory

sealed interface ProjectSearchListItem {
    data class CategorySection(val categories: List<ProjectCategory>, val selectedCategoryId: Int?) : ProjectSearchListItem
    data class ArticleHeader(val title: String, val count: Int) : ProjectSearchListItem
    data class ArticleRow(val article: Article) : ProjectSearchListItem
    data class LoadMoreFooter(val isLoading: Boolean, val canLoadMore: Boolean) : ProjectSearchListItem
}
