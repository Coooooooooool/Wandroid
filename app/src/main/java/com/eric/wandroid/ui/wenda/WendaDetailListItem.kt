package com.eric.wandroid.ui.wenda

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.WendaComment

sealed interface WendaDetailListItem {
    data class Summary(
        val article: Article,
        val isCollecting: Boolean
    ) : WendaDetailListItem

    data class Header(val count: Int) : WendaDetailListItem

    data class CommentRow(val comment: WendaComment) : WendaDetailListItem

    data class StateRow(
        val message: String,
        val actionLabel: String? = null
    ) : WendaDetailListItem

    data object LoadingRow : WendaDetailListItem
}
