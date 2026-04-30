package com.eric.wandroid.data.mapper

import com.eric.wandroid.data.remote.dto.UserMessageDto
import com.eric.wandroid.domain.model.UserMessage

fun UserMessageDto.toDomain(defaultIsRead: Boolean): UserMessage {
    return UserMessage(
        id = id ?: 0,
        title = title.orEmpty(),
        link = fullLink.orEmpty(),
        niceDate = niceDate.orEmpty(),
        fromUser = fromUser.orEmpty(),
        tag = tag.orEmpty(),
        content = message.orEmpty(),
        isRead = isRead == 1 || defaultIsRead
    )
}
