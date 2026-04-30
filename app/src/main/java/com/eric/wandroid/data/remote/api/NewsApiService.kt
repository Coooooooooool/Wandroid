package com.eric.wandroid.data.remote.api

import com.eric.wandroid.data.remote.dto.NewsContentResponseDto
import com.eric.wandroid.data.remote.dto.NewsListResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("toutiao/index")
    suspend fun getNewsList(
        @Query("type") type: String,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("is_filter") isFilter: Int,
        @Query("is_content") isContent: Int,
        @Query("key") key: String
    ): NewsListResponseDto

    @GET("toutiao/content")
    suspend fun getNewsContent(
        @Query("uniquekey") uniqueKey: String,
        @Query("key") key: String
    ): NewsContentResponseDto
}
