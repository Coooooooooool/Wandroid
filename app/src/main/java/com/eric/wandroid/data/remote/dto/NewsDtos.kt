package com.eric.wandroid.data.remote.dto

data class NewsListResponseDto(
    val reason: String?,
    val result: NewsListResultDto?,
    val error_code: Int?
)

data class NewsListResultDto(
    val stat: String?,
    val data: List<NewsArticleDto>?
)

data class NewsContentResponseDto(
    val reason: String?,
    val result: NewsContentDto?,
    val error_code: Int?
)

data class NewsArticleDto(
    val uniquekey: String?,
    val title: String?,
    val date: String?,
    val category: String?,
    val author_name: String?,
    val url: String?,
    val thumbnail_pic_s: String?,
    val thumbnail_pic_s02: String?,
    val thumbnail_pic_s03: String?,
    val is_content: String?
)

data class NewsContentDto(
    val uniquekey: String?,
    val title: String?,
    val date: String?,
    val category: String?,
    val author_name: String?,
    val url: String?,
    val thumbnail_pic_s: String?,
    val thumbnail_pic_s02: String?,
    val thumbnail_pic_s03: String?,
    val content: String?
)
