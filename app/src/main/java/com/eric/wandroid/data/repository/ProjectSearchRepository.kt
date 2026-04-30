package com.eric.wandroid.data.repository

import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.mapper.toDomain
import com.eric.wandroid.data.mapper.toProjectCategories
import com.eric.wandroid.data.remote.api.WanAndroidApiService
import com.eric.wandroid.data.remote.apiErrorCode
import com.eric.wandroid.data.remote.requireData
import com.eric.wandroid.data.remote.toRepositoryMessage
import com.eric.wandroid.domain.model.ProjectArticlePageData
import com.eric.wandroid.domain.model.ProjectCategory
import com.eric.wandroid.domain.model.ProjectOverviewData
import com.eric.wandroid.domain.model.SearchArticlePageData

class ProjectSearchRepository(
    private val apiService: WanAndroidApiService
) {
    suspend fun loadProjectOverview(selectedCategoryId: Int? = null): AppResult<ProjectOverviewData> {
        return try {
            val categories = apiService.getProjectCategories().requireData().toProjectCategories()
            val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
                ?: categories.firstOrNull()
            val pageDto = if (selectedCategory != null) {
                apiService.getProjectArticles(page = PROJECT_FIRST_PAGE, categoryId = selectedCategory.id).requireData()
            } else {
                null
            }

            AppResult.Success(
                ProjectOverviewData(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    articles = pageDto?.datas.orEmpty().map { it.toDomain() },
                    projectPage = PROJECT_FIRST_PAGE,
                    canLoadMore = pageDto?.over != true
                )
            )
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    suspend fun loadProjectArticles(
        category: ProjectCategory,
        page: Int
    ): AppResult<ProjectArticlePageData> {
        return try {
            val pageDto = apiService.getProjectArticles(page = page, categoryId = category.id).requireData()
            AppResult.Success(
                ProjectArticlePageData(
                    selectedCategory = category,
                    articles = pageDto.datas.orEmpty().map { it.toDomain() },
                    projectPage = page,
                    canLoadMore = pageDto.over != true
                )
            )
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    suspend fun searchArticles(
        keyword: String,
        page: Int
    ): AppResult<SearchArticlePageData> {
        return try {
            val normalizedKeyword = keyword.trim()
            val pageDto = apiService.searchArticles(page = page, keyword = normalizedKeyword).requireData()
            AppResult.Success(
                SearchArticlePageData(
                    keyword = normalizedKeyword,
                    articles = pageDto.datas.orEmpty().map { it.toDomain() },
                    searchPage = page,
                    canLoadMore = pageDto.over != true
                )
            )
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    companion object {
        const val PROJECT_FIRST_PAGE = 1
        const val SEARCH_FIRST_PAGE = 0
    }
}
