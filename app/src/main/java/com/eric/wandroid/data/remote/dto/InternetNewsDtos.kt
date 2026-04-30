package com.eric.wandroid.data.remote.dto

data class InternetNewsResponseDto(
    val error_code: Int?,
    val reason: String?,
    val result: InternetNewsResultDto?
)

data class InternetNewsResultDto(
    val stat: String?,
    val msg: String?,
    val curpage: Int?,
    val allnum: Int?,
    val newslist: List<InternetNewsItemDto>?
)

data class InternetNewsItemDto(
    val id: String?,
    val title: String?,
    val description: String?,
    val source: String?,
    val url: String?,
    val ctime: String?,
    val picUrl: String?
)
