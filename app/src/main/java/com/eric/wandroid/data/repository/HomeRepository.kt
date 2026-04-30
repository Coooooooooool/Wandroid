package com.eric.wandroid.data.repository

import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.mapper.toDomain
import com.eric.wandroid.data.mapper.toPopularRoute
import com.eric.wandroid.data.remote.apiErrorCode
import com.eric.wandroid.data.remote.requireData
import com.eric.wandroid.data.remote.toRepositoryMessage
import com.eric.wandroid.data.remote.api.WanAndroidApiService
import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.HomePageData
import com.eric.wandroid.domain.model.Website
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class HomeRepository(
    private val apiService: WanAndroidApiService
) {
    suspend fun loadHomePage(page: Int): AppResult<HomePageData> {
        return try {
            if (page == 0) {
                loadFirstPage()
            } else {
                loadMorePage(page)
            }
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    suspend fun loadCommonWebsites(): AppResult<List<Website>> {
        return try {
            AppResult.Success(
                apiService.getCommonWebsites()
                    .requireData()
                    .map { it.toDomain() }
            )
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    private suspend fun loadFirstPage(): AppResult<HomePageData> = coroutineScope {
        val bannersDeferred = async { apiService.getBanners().requireData() }
        val hotKeysDeferred = async { apiService.getHotKeys().requireData() }
        val websitesDeferred = async { apiService.getCommonWebsites().requireData() }
        val popularRoutesDeferred = async { apiService.getPopularRoutes().requireData() }
        val popularWendaDeferred = async { apiService.getPopularWenda().requireData() }
        val popularColumnsDeferred = async { apiService.getPopularColumns().requireData() }
        val topArticlesDeferred = async { apiService.getTopArticles().requireData() }
        val articlesDeferred = async { apiService.getHomeArticles(0).requireData() }

        val pageDto = articlesDeferred.await()
        val mergedArticles = mergeArticles(
            topArticles = topArticlesDeferred.await().map { it.toDomain(isTopPinned = true) },
            pageArticles = pageDto.datas.orEmpty().map { it.toDomain() }
        )

        AppResult.Success(
            HomePageData(
                page = 0,
                banners = bannersDeferred.await().map { it.toDomain() },
                hotKeys = hotKeysDeferred.await().map { it.toDomain() },
                websites = websitesDeferred.await().map { it.toDomain() },
                popularRoutes = popularRoutesDeferred.await().map { it.toPopularRoute() },
                popularWenda = popularWendaDeferred.await().map { it.toDomain() },
                popularColumns = popularColumnsDeferred.await().map { it.toDomain() },
                articles = mergedArticles,
                canLoadMore = pageDto.over != true
            )
        )
    }

    private suspend fun loadMorePage(page: Int): AppResult<HomePageData> {
        val pageDto = apiService.getHomeArticles(page).requireData()
        return AppResult.Success(
            HomePageData(
                page = page,
                banners = emptyList(),
                hotKeys = emptyList(),
                websites = emptyList(),
                popularRoutes = emptyList(),
                popularWenda = emptyList(),
                popularColumns = emptyList(),
                articles = pageDto.datas.orEmpty().map { it.toDomain() },
                canLoadMore = pageDto.over != true
            )
        )
    }

    private fun mergeArticles(
        topArticles: List<Article>,
        pageArticles: List<Article>
    ): List<Article> {
        val seenIds = linkedSetOf<Int>()
        val merged = ArrayList<Article>(topArticles.size + pageArticles.size)

        (topArticles + pageArticles).forEach { article ->
            if (seenIds.add(article.id)) {
                merged += article
            }
        }
        return merged
    }
}
