package com.eric.wandroid.data.remote.dto

data class ChapterDto(
    val id: Int?,
    val name: String?,
    val children: List<ChapterDto>?
)

data class NavigationDto(
    val name: String?,
    val articles: List<ArticleDto>?
)
