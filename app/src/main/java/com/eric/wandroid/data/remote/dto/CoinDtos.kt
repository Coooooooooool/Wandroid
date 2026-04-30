package com.eric.wandroid.data.remote.dto

data class CoinUserInfoDto(
    val coinCount: Int?,
    val rank: Int?,
    val userId: Int?,
    val username: String?
)

data class CoinRecordDto(
    val coinCount: Int?,
    val date: Long?,
    val desc: String?,
    val id: Int?,
    val reason: String?,
    val type: Int?,
    val userId: Int?,
    val userName: String?
)
