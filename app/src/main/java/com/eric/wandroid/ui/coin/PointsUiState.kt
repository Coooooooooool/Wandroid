package com.eric.wandroid.ui.coin

import com.eric.wandroid.domain.model.CoinOverview
import com.eric.wandroid.domain.model.CoinRecord

data class PointsUiState(
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val blockingErrorMessage: String? = null,
    val overview: CoinOverview? = null,
    val records: List<CoinRecord> = emptyList(),
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true
) {
    val hasContent: Boolean
        get() = overview != null || records.isNotEmpty()
}
