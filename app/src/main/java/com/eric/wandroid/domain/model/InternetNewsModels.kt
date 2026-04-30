package com.eric.wandroid.domain.model

data class InternetNewsArticle(
    val title: String,
    val summary: String,
    val source: String,
    val url: String,
    val time: String,
    val imageUrl: String
)

data class InternetNewsPage(
    val articles: List<InternetNewsArticle>,
    val hasMore: Boolean
)
