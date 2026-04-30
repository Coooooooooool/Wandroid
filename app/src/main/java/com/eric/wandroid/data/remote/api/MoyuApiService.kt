package com.eric.wandroid.data.remote.api

import com.eric.wandroid.data.remote.dto.XiaoJieJieVideoDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MoyuApiService {
    @GET("api/MP4_xiaojiejie")
    suspend fun getRandomVideo(
        @Query("type") type: String = "json"
    ): XiaoJieJieVideoDto
}
