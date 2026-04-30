package com.eric.wandroid.data.repository

import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.mapper.toDomain
import com.eric.wandroid.data.mapper.toNavigationGroups
import com.eric.wandroid.data.mapper.toSystemRoots
import com.eric.wandroid.data.remote.api.WanAndroidApiService
import com.eric.wandroid.data.remote.apiErrorCode
import com.eric.wandroid.data.remote.requireData
import com.eric.wandroid.data.remote.toRepositoryMessage
import com.eric.wandroid.domain.model.SystemArticlePageData
import com.eric.wandroid.domain.model.SystemChildCategory
import com.eric.wandroid.domain.model.SystemOverviewData
import com.eric.wandroid.domain.model.SystemRootCategory
import com.eric.wandroid.domain.model.SystemRootDetailData

class SystemRepository(
    private val apiService: WanAndroidApiService
) {
    suspend fun loadSystemRoots(): AppResult<List<SystemRootCategory>> {
        return try {
            AppResult.Success(apiService.getSystemTree().requireData().toSystemRoots())
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    suspend fun loadSystemOverview(selectedCategoryId: Int? = null): AppResult<SystemOverviewData> {
        return try {
            val roots = apiService.getSystemTree().requireData().toSystemRoots()
            val allCategories = roots.flatMap { it.children }
            val selectedCategory = allCategories.firstOrNull { it.id == selectedCategoryId }
                ?: allCategories.firstOrNull()

            val articlePage = if (selectedCategory != null) {
                apiService.getSystemArticles(page = 0, categoryId = selectedCategory.id).requireData()
            } else {
                null
            }

            AppResult.Success(
                SystemOverviewData(
                    roots = roots,
                    navigationGroups = emptyList(),
                    selectedCategory = selectedCategory,
                    articles = articlePage?.datas.orEmpty().map { it.toDomain() },
                    page = 0,
                    canLoadMore = articlePage?.over != true
                )
            )
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    suspend fun loadRootDetail(rootId: Int): AppResult<SystemRootDetailData> {
        return try {
            val roots = apiService.getSystemTree().requireData().toSystemRoots()
            val root = roots.firstOrNull { it.id == rootId }
                ?: return AppResult.Error("Request failed. Please try again later.")
            val selectedCategory = root.children.firstOrNull()
            val articlePage = if (selectedCategory != null) {
                apiService.getSystemArticles(page = 0, categoryId = selectedCategory.id).requireData()
            } else {
                null
            }

            AppResult.Success(
                SystemRootDetailData(
                    root = root,
                    selectedCategory = selectedCategory,
                    articles = articlePage?.datas.orEmpty().map { it.toDomain() },
                    page = 0,
                    canLoadMore = articlePage?.over != true
                )
            )
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    suspend fun loadNavigationGroups() = try {
        AppResult.Success(apiService.getNavigationItems().requireData().toNavigationGroups())
    } catch (exception: Throwable) {
        AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
    }

    suspend fun loadCategoryArticles(
        category: SystemChildCategory,
        page: Int
    ): AppResult<SystemArticlePageData> {
        return try {
            val pageDto = apiService.getSystemArticles(page = page, categoryId = category.id).requireData()
            AppResult.Success(
                SystemArticlePageData(
                    selectedCategory = category,
                    articles = pageDto.datas.orEmpty().map { it.toDomain() },
                    page = page,
                    canLoadMore = pageDto.over != true
                )
            )
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }
}
