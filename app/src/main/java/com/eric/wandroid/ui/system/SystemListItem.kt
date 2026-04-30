package com.eric.wandroid.ui.system

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.NavigationGroup
import com.eric.wandroid.domain.model.SystemRootCategory

sealed interface SystemListItem {
    data class ChapterGroup(val root: SystemRootCategory, val selectedCategoryId: Int?) : SystemListItem
    data class NavigationSectionHeader(val count: Int) : SystemListItem
    data class NavigationGroupRow(val group: NavigationGroup) : SystemListItem
    data class ArticleSectionHeader(val categoryName: String, val count: Int) : SystemListItem
    data class ArticleRow(val article: Article) : SystemListItem
    data class LoadMoreFooter(val isLoading: Boolean, val canLoadMore: Boolean) : SystemListItem
}
