package com.eric.wandroid.data.mapper

import com.eric.wandroid.data.remote.dto.TodoDto
import com.eric.wandroid.domain.model.TodoItem

fun TodoDto.toDomain(): TodoItem {
    return TodoItem(
        id = id ?: 0,
        title = title.orEmpty(),
        content = content.orEmpty(),
        dateText = dateStr.orEmpty(),
        completeDateText = completeDateStr.orEmpty(),
        status = status ?: 0,
        type = type ?: 3,
        priority = priority ?: 2
    )
}
