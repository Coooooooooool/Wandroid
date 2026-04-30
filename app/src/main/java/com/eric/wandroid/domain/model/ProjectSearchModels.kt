package com.eric.wandroid.domain.model

data class ProjectCategory(
    val id: Int,
    val name: String
)

data class ProjectOverviewData(
    val categories: List<ProjectCategory>,
    val selectedCategory: ProjectCategory?,
    val articles: List<Article>,
    val projectPage: Int,
    val canLoadMore: Boolean
)

data class ProjectArticlePageData(
    val selectedCategory: ProjectCategory,
    val articles: List<Article>,
    val projectPage: Int,
    val canLoadMore: Boolean
)

data class SearchArticlePageData(
    val keyword: String,
    val articles: List<Article>,
    val searchPage: Int,
    val canLoadMore: Boolean
)
