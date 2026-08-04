package com.eric.wandroid.ui.moyu.news

import androidx.annotation.StringRes
import com.eric.wandroid.R
import com.eric.wandroid.domain.model.NewsArticle
import com.eric.wandroid.domain.model.NewsDetail

data class NewsCategoryPageUiState(
    val categoryType: String,
    val articles: List<NewsArticle> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val blockingErrorMessage: String? = null
) {
    val hasContent: Boolean
        get() = articles.isNotEmpty()
}

interface NewsListStateLike {
    val articles: List<NewsArticle>
    val isLoadingMore: Boolean
    val canLoadMore: Boolean
}

data class NewsCategoryUiModel(
    val type: String,
    @StringRes val labelRes: Int
)

data class NewsDetailUiState(
    val article: NewsArticle,
    val detail: NewsDetail? = null,
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val blockingErrorMessage: String? = null
) {
    val hasContent: Boolean
        get() = detail != null
}

const val DEFAULT_NEWS_TYPE = "top"

fun defaultNewsCategories(): List<NewsCategoryUiModel> {
    return listOf(
        NewsCategoryUiModel("top", R.string.news_category_top),
        NewsCategoryUiModel("guonei", R.string.news_category_domestic),
        NewsCategoryUiModel("guoji", R.string.news_category_international),
        NewsCategoryUiModel("yule", R.string.news_category_entertainment),
        NewsCategoryUiModel("tiyu", R.string.news_category_sports),
        NewsCategoryUiModel("junshi", R.string.news_category_military),
        NewsCategoryUiModel("keji", R.string.news_category_technology),
        NewsCategoryUiModel("caijing", R.string.news_category_finance),
        NewsCategoryUiModel("youxi", R.string.news_category_games),
        NewsCategoryUiModel("qiche", R.string.news_category_automotive),
        NewsCategoryUiModel("jiankang", R.string.news_category_health)
    )
}
