package com.eric.wandroid.data.mapper

import com.eric.wandroid.data.remote.dto.CoinRecordDto
import com.eric.wandroid.data.remote.dto.CoinUserInfoDto
import com.eric.wandroid.domain.model.CoinOverview
import com.eric.wandroid.domain.model.CoinRecord
import com.eric.wandroid.domain.model.UserSession

fun CoinUserInfoDto.toDomain(session: UserSession?): CoinOverview {
    val resolvedUsername = username.orEmpty().ifBlank { session?.username.orEmpty() }
    val resolvedDisplayName = session?.displayName
        .orEmpty()
        .ifBlank { resolvedUsername }
        .ifBlank { "User" }

    return CoinOverview(
        userId = userId ?: session?.id ?: 0,
        username = resolvedUsername.ifBlank { resolvedDisplayName },
        displayName = resolvedDisplayName,
        coinCount = coinCount ?: 0,
        rank = rank ?: 0
    )
}

fun CoinRecordDto.toDomain(): CoinRecord {
    return CoinRecord(
        id = id ?: 0,
        coinCount = coinCount ?: 0,
        reason = reason.orEmpty(),
        description = desc.orEmpty(),
        date = date ?: 0L
    )
}
