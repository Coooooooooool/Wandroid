package com.eric.wandroid.data.repository

import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.api.MoeHuApiService
import com.eric.wandroid.data.remote.toRepositoryMessage
import kotlinx.coroutines.CancellationException

class MoeHuWallpaperRepository(
    private val apiService: MoeHuApiService
) {
    suspend fun loadWallpapers(categoryId: String): AppResult<List<String>> {
        return try {
            val response = apiService.getWallpapers(categoryId = categoryId)
            val urls = buildList {
                response.acgurl?.takeIf { it.isNotBlank() }?.let(::add)
                response.pic.orEmpty().filterTo(this) { it.isNotBlank() }
            }.distinct()

            if (response.code == "200" && urls.isNotEmpty()) {
                AppResult.Success(urls)
            } else {
                AppResult.Error("Wallpaper request failed.")
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage())
        }
    }
}
