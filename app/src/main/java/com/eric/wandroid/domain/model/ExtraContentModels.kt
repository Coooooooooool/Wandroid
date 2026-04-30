package com.eric.wandroid.domain.model

data class ExtraContentOverviewData(
    val wechatCategories: List<ProjectCategory>,
    val selectedWechatCategory: ProjectCategory?,
    val authorName: String = "",
    val articles: List<Article>,
    val page: Int,
    val canLoadMore: Boolean
)

data class ExtraContentArticlePageData(
    val articles: List<Article>,
    val page: Int,
    val canLoadMore: Boolean
)
