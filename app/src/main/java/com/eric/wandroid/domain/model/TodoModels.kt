package com.eric.wandroid.domain.model

data class TodoItem(
    val id: Int,
    val title: String,
    val content: String,
    val dateText: String,
    val completeDateText: String,
    val status: Int,
    val type: Int,
    val priority: Int
) {
    val isCompleted: Boolean
        get() = status == 1
}

data class TodoPageData(
    val page: Int,
    val todos: List<TodoItem>,
    val canLoadMore: Boolean
)
