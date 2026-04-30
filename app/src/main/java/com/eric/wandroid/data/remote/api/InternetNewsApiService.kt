package com.eric.wandroid.data.remote.api

import com.eric.wandroid.data.remote.dto.InternetNewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface InternetNewsApiService {
    @GET("fapigx/internet_news/query")
    suspend fun queryInternetNews(
        @Query("page") page: Int,
        @Query("num") pageSize: Int,
        @Query("rand") rand: Int,
        @Query("word") keyword: String,
        @Query("key") key: String
    ): InternetNewsResponseDto
}
