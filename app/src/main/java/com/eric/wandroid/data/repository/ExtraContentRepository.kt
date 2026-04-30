package com.eric.wandroid.data.repository

import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.mapper.toDomain
import com.eric.wandroid.data.mapper.toProjectCategories
import com.eric.wandroid.data.remote.api.WanAndroidApiService
import com.eric.wandroid.data.remote.apiErrorCode
import com.eric.wandroid.data.remote.requireData
import com.eric.wandroid.data.remote.requireSuccess
import com.eric.wandroid.data.remote.toRepositoryMessage
import com.eric.wandroid.domain.model.ExtraContentArticlePageData
import com.eric.wandroid.domain.model.ExtraContentOverviewData
import com.eric.wandroid.domain.model.ProjectCategory

class ExtraContentRepository(
    private val apiService: WanAndroidApiService
) {
    suspend fun loadOverview(
        mode: ExtraContentMode,
        selectedWechatCategoryId: Int? = null,
        shareUserId: Int? = null
    ): AppResult<ExtraContentOverviewData> {
        return try {
            when (mode) {
                ExtraContentMode.Wenda -> {
                    val page = WENDA_FIRST_PAGE
                    val pageDto = apiService.getWendaArticles(page).requireData()
                    AppResult.Success(
                        ExtraContentOverviewData(
                            wechatCategories = emptyList(),
                            selectedWechatCategory = null,
                            authorName = "",
                            articles = pageDto.datas.orEmpty().map { it.toDomain() },
                            page = page,
                            canLoadMore = pageDto.over != true
                        )
                    )
                }

                ExtraContentMode.Square -> {
                    val page = SQUARE_FIRST_PAGE
                    val pageDto = apiService.getSquareArticles(page).requireData()
                    AppResult.Success(
                        ExtraContentOverviewData(
                            wechatCategories = emptyList(),
                            selectedWechatCategory = null,
                            authorName = "",
                            articles = pageDto.datas.orEmpty().map { it.toDomain() },
                            page = page,
                            canLoadMore = pageDto.over != true
                        )
                    )
                }

                ExtraContentMode.Wechat -> {
                    val categories = apiService.getWechatCategories().requireData().toProjectCategories()
                    val selectedCategory = categories.firstOrNull { it.id == selectedWechatCategoryId }
                        ?: categories.firstOrNull()
                    val page = WECHAT_FIRST_PAGE
                    val pageDto = if (selectedCategory != null) {
                        apiService.getWechatArticles(selectedCategory.id, page).requireData()
                    } else {
                        null
                    }
                    AppResult.Success(
                        ExtraContentOverviewData(
                            wechatCategories = categories,
                            selectedWechatCategory = selectedCategory,
                            authorName = "",
                            articles = pageDto?.datas.orEmpty().map { it.toDomain() },
                            page = page,
                            canLoadMore = pageDto?.over != true
                        )
                    )
                }

                ExtraContentMode.LatestProject -> {
                    val page = LATEST_PROJECT_FIRST_PAGE
                    val pageDto = apiService.getLatestProjectArticles(page).requireData()
                    AppResult.Success(
                        ExtraContentOverviewData(
                            wechatCategories = emptyList(),
                            selectedWechatCategory = null,
                            authorName = "",
                            articles = pageDto.datas.orEmpty().map { it.toDomain() },
                            page = page,
                            canLoadMore = pageDto.over != true
                        )
                    )
                }

                ExtraContentMode.ShareUser -> {
                    val userId = shareUserId ?: return AppResult.Error("Missing share user id.")
                    val page = SHARE_USER_FIRST_PAGE
                    val dto = apiService.getShareUserArticles(userId, page).requireData()
                    val pageDto = dto.shareArticles
                    AppResult.Success(
                        ExtraContentOverviewData(
                            wechatCategories = emptyList(),
                            selectedWechatCategory = null,
                            authorName = dto.coinInfo?.username.orEmpty(),
                            articles = pageDto?.datas.orEmpty().map { it.toDomain() },
                            page = pageDto?.curPage ?: page,
                            canLoadMore = pageDto?.over != true
                        )
                    )
                }

                ExtraContentMode.PrivateShare -> {
                    val page = PRIVATE_SHARE_FIRST_PAGE
                    val pageDto = apiService.getPrivateSharedArticles(page).requireData()
                    AppResult.Success(
                        ExtraContentOverviewData(
                            wechatCategories = emptyList(),
                            selectedWechatCategory = null,
                            authorName = "",
                            articles = pageDto.datas.orEmpty().map { it.toDomain() },
                            page = pageDto.curPage ?: page,
                            canLoadMore = pageDto.over != true
                        )
                    )
                }
            }
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    suspend fun loadMore(
        mode: ExtraContentMode,
        page: Int,
        wechatCategory: ProjectCategory? = null,
        shareUserId: Int? = null
    ): AppResult<ExtraContentArticlePageData> {
        return try {
            val pageDto = when (mode) {
                ExtraContentMode.Wenda -> apiService.getWendaArticles(page).requireData()
                ExtraContentMode.Square -> apiService.getSquareArticles(page).requireData()
                ExtraContentMode.Wechat -> {
                    val category = wechatCategory ?: return AppResult.Error("No WeChat category selected.")
                    apiService.getWechatArticles(category.id, page).requireData()
                }

                ExtraContentMode.LatestProject -> apiService.getLatestProjectArticles(page).requireData()
                ExtraContentMode.ShareUser -> {
                    val userId = shareUserId ?: return AppResult.Error("Missing share user id.")
                    apiService.getShareUserArticles(userId, page).requireData().shareArticles
                        ?: return AppResult.Error("Share article page is empty.")
                }
                ExtraContentMode.PrivateShare -> apiService.getPrivateSharedArticles(page).requireData()
            }
            AppResult.Success(
                ExtraContentArticlePageData(
                    articles = pageDto.datas.orEmpty().map { it.toDomain() },
                    page = page,
                    canLoadMore = pageDto.over != true
                )
            )
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    suspend fun addSharedArticle(title: String, link: String): AppResult<Unit> {
        return try {
            apiService.addSharedArticle(title, link).requireSuccess()
            AppResult.Success(Unit)
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    suspend fun deletePrivateSharedArticle(articleId: Int): AppResult<Unit> {
        return try {
            apiService.deletePrivateSharedArticle(articleId).requireSuccess()
            AppResult.Success(Unit)
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }

    companion object {
        const val WENDA_FIRST_PAGE = 1
        const val SQUARE_FIRST_PAGE = 0
        const val WECHAT_FIRST_PAGE = 1
        const val LATEST_PROJECT_FIRST_PAGE = 0
        const val SHARE_USER_FIRST_PAGE = 1
        const val PRIVATE_SHARE_FIRST_PAGE = 1
    }
}

enum class ExtraContentMode {
    Wenda,
    Square,
    Wechat,
    LatestProject,
    ShareUser,
    PrivateShare
}
