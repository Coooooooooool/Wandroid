package com.eric.wandroid.data.remote.dto

data class TodoDto(
    val id: Int?,
    val title: String?,
    val content: String?,
    val date: Any?,
    val dateStr: String?,
    val completeDate: Any?,
    val completeDateStr: String?,
    val status: Int?,
    val type: Int?,
    val priority: Int?,
    val userId: Int?
)
