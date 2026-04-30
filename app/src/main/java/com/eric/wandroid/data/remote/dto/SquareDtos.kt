package com.eric.wandroid.data.remote.dto

data class ShareUserArticlesDto(
    val coinInfo: CoinUserInfoDto?,
    val shareArticles: PageDto<ArticleDto>?
)
