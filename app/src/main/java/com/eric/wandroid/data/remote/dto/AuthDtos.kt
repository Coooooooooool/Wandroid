package com.eric.wandroid.data.remote.dto

data class UserDto(
    val id: Int?,
    val username: String?,
    val nickname: String?,
    val publicName: String?,
    val email: String?,
    val icon: String?,
    val type: Int?,
    val admin: Boolean?
)
