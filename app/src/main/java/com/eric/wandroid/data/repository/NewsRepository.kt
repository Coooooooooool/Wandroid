package com.eric.wandroid.data.repository

import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.api.NewsApiService
import com.eric.wandroid.data.remote.dto.NewsArticleDto
import com.eric.wandroid.data.remote.dto.NewsContentDto
import com.eric.wandroid.data.remote.toRepositoryMessage
import com.eric.wandroid.domain.model.NewsArticle
import com.eric.wandroid.domain.model.NewsDetail
import com.eric.wandroid.domain.model.NewsPage

class NewsRepository(
    private val apiService: NewsApiService,
    private val appKey: String = APP_KEY
) {
    suspend fun loadNewsList(
        type: String,
        page: Int,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): AppResult<NewsPage> {
        return try {
            val response = apiService.getNewsList(
                type = type,
                page = page,
                pageSize = pageSize,
                isFilter = 1,
                isContent = 0,
                key = appKey
            )
            val errorCode = response.error_code ?: -1
            if (errorCode != 0) {
                AppResult.Error(response.reason?.ifBlank { null } ?: "News request failed.", errorCode)
            } else {
                val items = response.result?.data.orEmpty()
                    .mapNotNull { it.toDomainOrNull() }
                AppResult.Success(
                    NewsPage(
                        articles = items,
                        hasMore = items.size >= pageSize
                    )
                )
            }
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage())
        }
    }

    suspend fun loadNewsDetail(article: NewsArticle): AppResult<NewsDetail> {
        if (article.uniqueKey.isBlank()) {
            return AppResult.Error("Missing article identifier.")
        }
        return try {
            val response = apiService.getNewsContent(
                uniqueKey = article.uniqueKey,
                key = appKey
            )
            val errorCode = response.error_code ?: -1
            if (errorCode != 0) {
                AppResult.Error(response.reason?.ifBlank { null } ?: "News detail request failed.", errorCode)
            } else {
                val detailDto = response.result
                val detailArticle = detailDto?.mergeWith(article) ?: article
                AppResult.Success(
                    NewsDetail(
                        article = detailArticle,
                        contentHtml = detailDto?.content.orEmpty().trim()
                    )
                )
            }
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage())
        }
    }

    private fun NewsArticleDto.toDomainOrNull(): NewsArticle? {
        val uniqueKeyValue = uniquekey.orEmpty().trim()
        val titleValue = title.orEmpty().trim()
        if (uniqueKeyValue.isBlank() || titleValue.isBlank()) return null
        return NewsArticle(
            uniqueKey = uniqueKeyValue,
            title = titleValue,
            date = date.orEmpty().trim(),
            category = category.orEmpty().trim(),
            authorName = author_name.orEmpty().trim(),
            url = url.orEmpty().trim(),
            thumbnailPicS = thumbnail_pic_s.orEmpty().trim(),
            thumbnailPicS02 = thumbnail_pic_s02.orEmpty().trim(),
            thumbnailPicS03 = thumbnail_pic_s03.orEmpty().trim(),
            hasContent = is_content == "1"
        )
    }

    private fun NewsContentDto.mergeWith(article: NewsArticle): NewsArticle {
        return article.copy(
            title = title.orEmpty().trim().ifBlank { article.title },
            date = date.orEmpty().trim().ifBlank { article.date },
            category = category.orEmpty().trim().ifBlank { article.category },
            authorName = author_name.orEmpty().trim().ifBlank { article.authorName },
            url = url.orEmpty().trim().ifBlank { article.url },
            thumbnailPicS = thumbnail_pic_s.orEmpty().trim().ifBlank { article.thumbnailPicS },
            thumbnailPicS02 = thumbnail_pic_s02.orEmpty().trim().ifBlank { article.thumbnailPicS02 },
            thumbnailPicS03 = thumbnail_pic_s03.orEmpty().trim().ifBlank { article.thumbnailPicS03 },
            hasContent = content.orEmpty().isNotBlank() || article.hasContent
        )
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        private const val APP_KEY = "a1ef310522ae51444677f3049d632e23"
    }
}
