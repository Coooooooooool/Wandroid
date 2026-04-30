package com.eric.wandroid.domain.model

data class SystemRootCategory(
    val id: Int,
    val name: String,
    val children: List<SystemChildCategory>
)

data class SystemChildCategory(
    val id: Int,
    val name: String,
    val parentName: String
)

data class NavigationGroup(
    val name: String,
    val articles: List<NavigationArticle>
)

data class NavigationArticle(
    val id: Int,
    val title: String,
    val link: String
)

data class SystemOverviewData(
    val roots: List<SystemRootCategory>,
    val navigationGroups: List<NavigationGroup>,
    val selectedCategory: SystemChildCategory?,
    val articles: List<Article>,
    val page: Int,
    val canLoadMore: Boolean
)

data class SystemArticlePageData(
    val selectedCategory: SystemChildCategory,
    val articles: List<Article>,
    val page: Int,
    val canLoadMore: Boolean
)

data class SystemRootDetailData(
    val root: SystemRootCategory,
    val selectedCategory: SystemChildCategory?,
    val articles: List<Article>,
    val page: Int,
    val canLoadMore: Boolean
)
