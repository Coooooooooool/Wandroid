package com.eric.wandroid.domain.model

data class SearchHistoryItem(
    val keyword: String,
    val timestamp: Long
)

data class ReadingHistoryItem(
    val title: String,
    val url: String,
    val timestamp: Long
)
