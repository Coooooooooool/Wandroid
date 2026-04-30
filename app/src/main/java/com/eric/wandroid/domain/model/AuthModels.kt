package com.eric.wandroid.domain.model

data class UserSession(
    val id: Int,
    val username: String,
    val displayName: String,
    val email: String,
    val isLoggedIn: Boolean
)
