package com.eric.wandroid.ui.todo

data class TodoEditorValue(
    val title: String,
    val content: String,
    val date: String,
    val type: Int,
    val priority: Int
)
