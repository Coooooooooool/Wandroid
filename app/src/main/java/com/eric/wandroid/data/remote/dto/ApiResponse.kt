package com.eric.wandroid.data.remote.dto

data class ApiResponse<T>(
    val data: T?,
    val errorCode: Int,
    val errorMsg: String
)
