package com.eric.wandroid.domain.model

data class CoinOverview(
    val userId: Int,
    val username: String,
    val displayName: String,
    val coinCount: Int,
    val rank: Int
)

data class CoinRecord(
    val id: Int,
    val coinCount: Int,
    val reason: String,
    val description: String,
    val date: Long
)

data class CoinRecordPageData(
    val page: Int,
    val records: List<CoinRecord>,
    val canLoadMore: Boolean
)
