package com.eric.wandroid.data.mapper

import com.eric.wandroid.data.remote.dto.ChapterDto
import com.eric.wandroid.data.remote.dto.NavigationDto
import com.eric.wandroid.domain.model.NavigationArticle
import com.eric.wandroid.domain.model.NavigationGroup
import com.eric.wandroid.domain.model.SystemChildCategory
import com.eric.wandroid.domain.model.SystemRootCategory

fun List<ChapterDto>.toSystemRoots(): List<SystemRootCategory> {
    return mapNotNull { root ->
        val rootId = root.id ?: return@mapNotNull null
        val rootName = root.name.orEmpty().ifBlank { return@mapNotNull null }
        val children = root.children.orEmpty()
            .mapNotNull { child ->
                val childId = child.id ?: return@mapNotNull null
                val childName = child.name.orEmpty().ifBlank { return@mapNotNull null }
                SystemChildCategory(
                    id = childId,
                    name = childName,
                    parentName = rootName
                )
            }
        val resolvedChildren = if (children.isEmpty()) {
            listOf(
                SystemChildCategory(
                    id = rootId,
                    name = rootName,
                    parentName = rootName
                )
            )
        } else {
            children
        }
        SystemRootCategory(
            id = rootId,
            name = rootName,
            children = resolvedChildren
        )
    }
}

fun List<NavigationDto>.toNavigationGroups(): List<NavigationGroup> {
    return mapNotNull { item ->
        val groupName = item.name.orEmpty().ifBlank { return@mapNotNull null }
        val articles = item.articles.orEmpty().map { article ->
            NavigationArticle(
                id = article.id ?: 0,
                title = article.title.orEmpty(),
                link = article.link.orEmpty()
            )
        }
        NavigationGroup(name = groupName, articles = articles)
    }
}
