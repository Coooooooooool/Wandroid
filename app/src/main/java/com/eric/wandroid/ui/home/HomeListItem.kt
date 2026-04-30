package com.eric.wandroid.ui.home

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.Banner
import com.eric.wandroid.domain.model.PopularColumn
import com.eric.wandroid.domain.model.PopularRoute

sealed interface HomeListItem {
    data object SearchEntry : HomeListItem
    data class BannerSection(val banners: List<Banner>) : HomeListItem
    data class PopularSection(
        val routes: List<PopularRoute>,
        val wenda: List<Article>,
        val columns: List<PopularColumn>
    ) : HomeListItem
    data class ArticleHeader(val count: Int) : HomeListItem
    data class ArticleRow(val article: Article) : HomeListItem
    data class LoadMoreFooter(val isLoading: Boolean, val canLoadMore: Boolean) : HomeListItem
}
