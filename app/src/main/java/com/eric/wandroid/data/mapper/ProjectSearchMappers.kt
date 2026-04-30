package com.eric.wandroid.data.mapper

import com.eric.wandroid.data.remote.dto.ChapterDto
import com.eric.wandroid.domain.model.ProjectCategory

fun List<ChapterDto>.toProjectCategories(): List<ProjectCategory> {
    return mapNotNull { chapter ->
        val id = chapter.id ?: return@mapNotNull null
        val name = chapter.name.orEmpty().ifBlank { return@mapNotNull null }
        ProjectCategory(id = id, name = name)
    }
}
