package com.eric.wandroid.data.mapper

import com.eric.wandroid.data.remote.dto.ArticleDto
import com.eric.wandroid.data.remote.dto.BannerDto
import com.eric.wandroid.data.remote.dto.HotKeyDto
import com.eric.wandroid.data.remote.dto.ChapterDto
import com.eric.wandroid.data.remote.dto.PopularColumnDto
import com.eric.wandroid.data.remote.dto.WebsiteDto
import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.Banner
import com.eric.wandroid.domain.model.HotKey
import com.eric.wandroid.domain.model.PopularColumn
import com.eric.wandroid.domain.model.PopularRoute
import com.eric.wandroid.domain.model.Website

fun ArticleDto.toDomain(isTopPinned: Boolean = false): Article {
    val displayAuthor = author.orEmpty().ifBlank { shareUser.orEmpty() }.ifBlank { "Anonymous" }
    return Article(
        id = id ?: 0,
        userId = userId ?: 0,
        title = title.orEmpty(),
        link = link.orEmpty(),
        author = displayAuthor,
        shareUser = shareUser.orEmpty(),
        chapterName = chapterName.orEmpty(),
        superChapterName = superChapterName.orEmpty(),
        niceDate = niceDate.orEmpty(),
        desc = desc.orEmpty(),
        isCollected = collect ?: false,
        isTopPinned = isTopPinned
    )
}

fun BannerDto.toDomain(): Banner = Banner(
    id = id ?: 0,
    title = title.orEmpty(),
    desc = desc.orEmpty(),
    imagePath = imagePath.orEmpty(),
    url = url.orEmpty()
)

fun HotKeyDto.toDomain(): HotKey = HotKey(
    id = id ?: 0,
    name = name.orEmpty(),
    link = link.orEmpty()
)

fun WebsiteDto.toDomain(): Website = Website(
    id = id ?: 0,
    name = name.orEmpty(),
    link = link.orEmpty(),
    category = category.orEmpty()
)

fun ChapterDto.toPopularRoute(): PopularRoute = PopularRoute(
    id = id ?: 0,
    name = name.orEmpty()
)

fun PopularColumnDto.toDomain(): PopularColumn = PopularColumn(
    id = id ?: 0,
    name = name.orEmpty(),
    url = url.orEmpty()
)
