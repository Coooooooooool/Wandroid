package com.eric.wandroid.ui.profile

import com.eric.wandroid.domain.model.Article

sealed interface CollectProfileListItem {
    data class FavoritesHeader(val count: Int) : CollectProfileListItem
    data class ArticleRow(val article: Article) : CollectProfileListItem
    data class LoadMoreFooter(val isLoading: Boolean, val canLoadMore: Boolean) : CollectProfileListItem
}
