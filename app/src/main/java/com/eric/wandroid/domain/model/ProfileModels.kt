package com.eric.wandroid.domain.model

data class UserProfile(
    val id: Int,
    val username: String,
    val displayName: String,
    val email: String,
    val coinCount: Int,
    val level: Int,
    val rank: String
)

data class CollectedArticlePageData(
    val page: Int,
    val articles: List<Article>,
    val canLoadMore: Boolean
)
