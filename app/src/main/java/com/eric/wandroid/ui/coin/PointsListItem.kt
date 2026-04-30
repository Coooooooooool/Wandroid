package com.eric.wandroid.ui.coin

import com.eric.wandroid.domain.model.CoinOverview
import com.eric.wandroid.domain.model.CoinRecord

sealed interface PointsListItem {
    data class Summary(val overview: CoinOverview) : PointsListItem
    data class RecordsHeader(val count: Int) : PointsListItem
    data class RecordRow(val record: CoinRecord) : PointsListItem
    data class LoadMoreFooter(val isLoading: Boolean, val canLoadMore: Boolean) : PointsListItem
}
