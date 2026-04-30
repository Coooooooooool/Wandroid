package com.eric.wandroid.data.repository

import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.mapper.toDomain
import com.eric.wandroid.data.remote.api.WanAndroidApiService
import com.eric.wandroid.data.remote.apiErrorCode
import com.eric.wandroid.data.remote.requireData
import com.eric.wandroid.data.remote.toRepositoryMessage
import com.eric.wandroid.domain.model.WendaComment

class WendaRepository(
    private val apiService: WanAndroidApiService
) {
    suspend fun loadComments(articleId: Int): AppResult<List<WendaComment>> {
        return try {
            val pageDto = apiService.getWendaComments(articleId).requireData()
            AppResult.Success(pageDto.datas.orEmpty().map { it.toDomain() })
        } catch (exception: Throwable) {
            AppResult.Error(exception.toRepositoryMessage(), exception.apiErrorCode())
        }
    }
}
