package com.eric.wandroid.ui.wenda

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.WendaComment

data class WendaDetailUiState(
    val article: Article,
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isCollecting: Boolean = false,
    val comments: List<WendaComment> = emptyList(),
    val commentsErrorMessage: String? = null
)
