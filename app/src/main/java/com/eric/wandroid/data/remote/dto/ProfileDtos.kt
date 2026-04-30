package com.eric.wandroid.data.remote.dto

data class UserInfoResponseDto(
    val coinInfo: CoinInfoDto?,
    val userInfo: UserInfoDetailDto?
)

data class CoinInfoDto(
    val coinCount: Int?,
    val level: Int?,
    val rank: String?,
    val userId: Int?,
    val username: String?,
    val nickname: String?
)

data class UserInfoDetailDto(
    val id: Int?,
    val username: String?,
    val nickname: String?,
    val publicName: String?,
    val email: String?,
    val coinCount: Int?,
    val icon: String?,
    val type: Int?,
    val admin: Boolean?
)

data class UserMessageDto(
    val id: Int?,
    val title: String?,
    val fullLink: String?,
    val niceDate: String?,
    val fromUser: String?,
    val tag: String?,
    val message: String?,
    val isRead: Int?
)
