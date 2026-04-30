package com.eric.wandroid.data.mapper

import com.eric.wandroid.data.remote.dto.UserInfoResponseDto
import com.eric.wandroid.domain.model.UserProfile

fun UserInfoResponseDto.toDomain(): UserProfile {
    val coinInfo = coinInfo
    val userInfo = userInfo
    val username = userInfo?.username
        .orEmpty()
        .ifBlank { coinInfo?.username.orEmpty() }
    val displayName = userInfo?.nickname
        .orEmpty()
        .ifBlank { userInfo?.publicName.orEmpty() }
        .ifBlank { coinInfo?.nickname.orEmpty() }
        .ifBlank { username }
        .ifBlank { "User" }

    return UserProfile(
        id = userInfo?.id ?: coinInfo?.userId ?: 0,
        username = username.ifBlank { displayName },
        displayName = displayName,
        email = userInfo?.email.orEmpty(),
        coinCount = userInfo?.coinCount ?: coinInfo?.coinCount ?: 0,
        level = coinInfo?.level ?: 0,
        rank = coinInfo?.rank.orEmpty()
    )
}
