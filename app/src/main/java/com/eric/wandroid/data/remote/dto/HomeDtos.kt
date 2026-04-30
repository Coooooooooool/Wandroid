package com.eric.wandroid.data.remote.dto

data class PageDto<T>(
    val curPage: Int?,
    val datas: List<T>?,
    val offset: Int?,
    val over: Boolean?,
    val pageCount: Int?,
    val size: Int?,
    val total: Int?
)

data class ArticleDto(
    val id: Int?,
    val userId: Int?,
    val title: String?,
    val link: String?,
    val author: String?,
    val shareUser: String?,
    val chapterName: String?,
    val superChapterName: String?,
    val niceDate: String?,
    val publishTime: Long?,
    val collect: Boolean?,
    val envelopePic: String?,
    val desc: String?
)

data class BannerDto(
    val id: Int?,
    val title: String?,
    val desc: String?,
    val imagePath: String?,
    val url: String?
)

data class HotKeyDto(
    val id: Int?,
    val name: String?,
    val link: String?
)

data class WebsiteDto(
    val id: Int?,
    val name: String?,
    val link: String?,
    val category: String?
)
