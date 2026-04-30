package com.eric.wandroid.data.mapper

import com.eric.wandroid.data.remote.dto.UserDto
import com.eric.wandroid.domain.model.UserSession

fun UserDto.toSession(): UserSession {
    val resolvedUsername = username.orEmpty()
    val displayName = nickname.orEmpty()
        .ifBlank { publicName.orEmpty() }
        .ifBlank { resolvedUsername }
        .ifBlank { "User" }
    return UserSession(
        id = id ?: 0,
        username = resolvedUsername,
        displayName = displayName,
        email = email.orEmpty(),
        isLoggedIn = true
    )
}
