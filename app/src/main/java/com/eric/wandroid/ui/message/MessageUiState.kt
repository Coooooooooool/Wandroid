package com.eric.wandroid.ui.message

import com.eric.wandroid.domain.model.MessageOverview
import com.eric.wandroid.domain.model.UserMessage

data class MessageUiState(
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val blockingErrorMessage: String? = null,
    val overview: MessageOverview? = null,
    val selectedFilter: MessageFilter = MessageFilter.Unread,
    val messages: List<UserMessage> = emptyList(),
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true
) {
    val hasContent: Boolean
        get() = overview != null || messages.isNotEmpty()
}
