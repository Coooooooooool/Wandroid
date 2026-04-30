package com.eric.wandroid.domain.model

data class NewsArticle(
    val uniqueKey: String,
    val title: String,
    val date: String,
    val category: String,
    val authorName: String,
    val url: String,
    val thumbnailPicS: String,
    val thumbnailPicS02: String,
    val thumbnailPicS03: String,
    val hasContent: Boolean
) {
    val previewImageUrl: String
        get() = listOf(thumbnailPicS, thumbnailPicS02, thumbnailPicS03)
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
}

data class NewsDetail(
    val article: NewsArticle,
    val contentHtml: String
)

data class NewsPage(
    val articles: List<NewsArticle>,
    val hasMore: Boolean
)
