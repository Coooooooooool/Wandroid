package com.eric.wandroid.ui.message

import com.eric.wandroid.domain.model.MessageOverview
import com.eric.wandroid.domain.model.UserMessage

sealed interface MessageListItem {
    data class Summary(val overview: MessageOverview) : MessageListItem
    data class FilterSection(val selectedFilter: MessageFilter) : MessageListItem
    data class Header(val count: Int) : MessageListItem
    data class MessageRow(val message: UserMessage) : MessageListItem
    data class LoadMoreFooter(val isLoading: Boolean, val canLoadMore: Boolean) : MessageListItem
}
