package com.eric.wandroid.domain.model

data class MessageOverview(
    val unreadCount: Int
)

data class UserMessage(
    val id: Int,
    val title: String,
    val link: String,
    val niceDate: String,
    val fromUser: String,
    val tag: String,
    val content: String,
    val isRead: Boolean
)

data class UserMessagePageData(
    val page: Int,
    val messages: List<UserMessage>,
    val canLoadMore: Boolean
)
