package com.eric.wandroid.domain.model

data class WendaComment(
    val id: Int,
    val articleId: Int,
    val userName: String,
    val toUserName: String,
    val niceDate: String,
    val content: String,
    val contentMd: String,
    val likeCount: Int,
    val isAnonymous: Boolean,
    val canEdit: Boolean,
    val replies: List<WendaComment>
)
