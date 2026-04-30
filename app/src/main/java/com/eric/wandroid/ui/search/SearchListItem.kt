package com.eric.wandroid.ui.search

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.HotKey
import com.eric.wandroid.domain.model.SearchHistoryItem

sealed interface SearchListItem {
    data class SearchBox(val keyword: String, val isResultMode: Boolean) : SearchListItem
    data class SearchHistorySection(val history: List<SearchHistoryItem>) : SearchListItem
    data class HotKeySection(val hotKeys: List<HotKey>) : SearchListItem
    data class ResultHeader(val keyword: String, val count: Int) : SearchListItem
    data class ArticleRow(val article: Article) : SearchListItem
    data class LoadMoreFooter(val isLoading: Boolean, val canLoadMore: Boolean) : SearchListItem
}
