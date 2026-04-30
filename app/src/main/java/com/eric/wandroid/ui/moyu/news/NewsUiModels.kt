package com.eric.wandroid.ui.moyu.news

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
    val label: String
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
        NewsCategoryUiModel("top", "头条"),
        NewsCategoryUiModel("guonei", "国内"),
        NewsCategoryUiModel("guoji", "国际"),
        NewsCategoryUiModel("yule", "娱乐"),
        NewsCategoryUiModel("tiyu", "体育"),
        NewsCategoryUiModel("junshi", "军事"),
        NewsCategoryUiModel("keji", "科技"),
        NewsCategoryUiModel("caijing", "财经"),
        NewsCategoryUiModel("youxi", "游戏"),
        NewsCategoryUiModel("qiche", "汽车"),
        NewsCategoryUiModel("jiankang", "健康")
    )
}
