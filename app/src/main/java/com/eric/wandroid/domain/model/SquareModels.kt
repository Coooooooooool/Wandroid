package com.eric.wandroid.domain.model

data class ShareUserArticlesPageData(
    val page: Int,
    val authorName: String,
    val articles: List<Article>,
    val canLoadMore: Boolean
)

data class PrivateShareArticlePageData(
    val page: Int,
    val articles: List<Article>,
    val canLoadMore: Boolean
)
