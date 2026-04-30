package com.eric.wandroid.ui.todo

import com.eric.wandroid.domain.model.TodoItem

sealed interface TodoListItem {
    data class FilterSection(val selectedFilter: TodoFilter) : TodoListItem
    data class Header(val count: Int) : TodoListItem
    data class EmptyHint(val selectedFilter: TodoFilter) : TodoListItem
    data class TodoRow(val todo: TodoItem) : TodoListItem
    data class LoadMoreFooter(val isLoading: Boolean, val canLoadMore: Boolean) : TodoListItem
}
