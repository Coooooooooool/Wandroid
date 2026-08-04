package com.eric.wandroid.data.remote.api

import com.eric.wandroid.data.remote.dto.MoeHuWallpaperResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MoeHuApiService {
    @GET("pic.php")
    suspend fun getWallpapers(
        @Query("return") returnType: String = "json",
        @Query("id") categoryId: String,
        @Query("num") count: Int = 30,
        @Query("size") size: String = "mw690",
        @Query("cdn") cdn: String = "cf"
    ): MoeHuWallpaperResponse
}
