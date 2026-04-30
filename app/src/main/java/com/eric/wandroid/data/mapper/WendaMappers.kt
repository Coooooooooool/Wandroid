package com.eric.wandroid.data.mapper

import com.eric.wandroid.data.remote.dto.WendaCommentDto
import com.eric.wandroid.domain.model.WendaComment

fun WendaCommentDto.toDomain(): WendaComment = WendaComment(
    id = id ?: 0,
    articleId = articleId ?: 0,
    userName = userName.orEmpty(),
    toUserName = toUserName.orEmpty(),
    niceDate = niceDate.orEmpty(),
    content = content.orEmpty(),
    contentMd = contentMd.orEmpty(),
    likeCount = zan ?: 0,
    isAnonymous = anonymous == 1,
    canEdit = canEdit == true,
    replies = replyComments.orEmpty().map { it.toDomain() }
)
