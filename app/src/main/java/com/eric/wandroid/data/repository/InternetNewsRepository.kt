package com.eric.wandroid.data.repository

import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.api.InternetNewsApiService
import com.eric.wandroid.data.remote.dto.InternetNewsItemDto
import com.eric.wandroid.data.remote.toRepositoryMessage
import com.eric.wandroid.domain.model.InternetNewsArticle
import com.eric.wandroid.domain.model.InternetNewsPage

class InternetNewsRepository(
    private val apiService: InternetNewsApiService,
    private val appKey: String = APP_KEY
) {
    suspend fun queryNews(
        keyword: String,
        page: Int,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): AppResult<InternetNewsPage> {
        return try {
            val response = apiService.queryInternetNews(
                page = page,
                pageSize = pageSize,
                rand = 0,
                keyword = keyword.trim(),
                key = appKey
            )
            val errorCode = response.error_code ?: -1
            if (errorCode != 0) {
                AppResult.Error(response.reason?.ifBlank { null } ?: "Internet news request failed.", errorCode)
            } else {
                val items = response.result?.newslist.orEmpty()
                    .mapNotNull { it.toDomainOrNull() }
                AppResult.Success(
                    InternetNewsPage(
                        articles = items,
                        hasMore = items.size >= pageSize
                    )
                )
            }
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage())
        }
    }

    private fun InternetNewsItemDto.toDomainOrNull(): InternetNewsArticle? {
        val titleValue = title.orEmpty().trim()
        val urlValue = url.orEmpty().trim()
        if (titleValue.isBlank() || urlValue.isBlank()) return null
        return InternetNewsArticle(
            title = titleValue,
            summary = description.orEmpty().trim(),
            source = source.orEmpty().trim(),
            url = urlValue,
            time = ctime.orEmpty().trim(),
            imageUrl = picUrl.orEmpty().trim()
        )
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        private const val APP_KEY = "4b122353c90ccac74a85347a02134d2c"
    }
}
