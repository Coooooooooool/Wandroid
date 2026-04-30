package com.eric.wandroid.domain.model

data class Article(
    val id: Int,
    val userId: Int,
    val title: String,
    val link: String,
    val author: String,
    val shareUser: String,
    val chapterName: String,
    val superChapterName: String,
    val niceDate: String,
    val desc: String,
    val isCollected: Boolean,
    val isTopPinned: Boolean
)

data class Banner(
    val id: Int,
    val title: String,
    val desc: String,
    val imagePath: String,
    val url: String
)

data class HotKey(
    val id: Int,
    val name: String,
    val link: String
)

data class Website(
    val id: Int,
    val name: String,
    val link: String,
    val category: String
)

data class PopularRoute(
    val id: Int,
    val name: String
)

data class PopularColumn(
    val id: Int,
    val name: String,
    val url: String
)

data class HomePageData(
    val page: Int,
    val banners: List<Banner>,
    val hotKeys: List<HotKey>,
    val websites: List<Website>,
    val popularRoutes: List<PopularRoute>,
    val popularWenda: List<Article>,
    val popularColumns: List<PopularColumn>,
    val articles: List<Article>,
    val canLoadMore: Boolean
)
