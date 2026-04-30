package com.eric.wandroid.ui.todo

import com.eric.wandroid.domain.model.TodoItem

data class TodoUiState(
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isSubmittingEditor: Boolean = false,
    val hasLoadedOnce: Boolean = false,
    val blockingErrorMessage: String? = null,
    val selectedFilter: TodoFilter = TodoFilter.All,
    val todos: List<TodoItem> = emptyList(),
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val actingTodoIds: Set<Int> = emptySet()
) {
    val hasContent: Boolean
        get() = todos.isNotEmpty()
}
