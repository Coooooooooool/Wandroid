package com.eric.wandroid.data.remote.dto

data class WendaCommentDto(
    val anonymous: Int?,
    val appendForContent: Int?,
    val articleId: Int?,
    val canEdit: Boolean?,
    val content: String?,
    val contentMd: String?,
    val id: Int?,
    val niceDate: String?,
    val publishDate: Long?,
    val replyCommentId: Int?,
    val replyComments: List<WendaCommentDto>?,
    val rootCommentId: Int?,
    val status: Int?,
    val toUserId: Int?,
    val toUserName: String?,
    val userId: Int?,
    val userName: String?,
    val zan: Int?
)
