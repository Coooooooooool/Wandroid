package com.eric.wandroid.data.repository

import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.api.MoyuApiService
import com.eric.wandroid.data.remote.dto.XiaoJieJieVideoDto
import com.eric.wandroid.data.remote.toRepositoryMessage

class MoyuRepository(
    private val apiService: MoyuApiService
) {
    suspend fun loadRandomVideo(): AppResult<String> {
        return try {
            val dto = apiService.getRandomVideo()
            when {
                dto.code == 200 && !dto.mp4_video.isNullOrBlank() -> {
                    AppResult.Success(dto.mp4_video)
                }

                else -> {
                    AppResult.Error(dto.msg?.takeIf { it.isNotBlank() } ?: "Video request failed.")
                }
            }
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage())
        }
    }
}
