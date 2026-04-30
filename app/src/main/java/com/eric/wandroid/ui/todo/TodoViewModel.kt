package com.eric.wandroid.ui.todo

import com.eric.wandroid.common.auth.LOGIN_EXPIRED_CODE
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.AccountRepository
import com.eric.wandroid.domain.model.TodoItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodoViewModel(
    private val repository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _authRequired = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val authRequired: SharedFlow<String> = _authRequired.asSharedFlow()

    init {
        requestTodos(isRefresh = false)
    }

    fun refresh() {
        if (isBusy()) return
        requestTodos(isRefresh = true)
    }

    fun retry() {
        if (isBusy()) return
        requestTodos(isRefresh = false)
    }

    fun selectFilter(filter: TodoFilter) {
        if (isBusy() || _uiState.value.selectedFilter == filter) return
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                currentPage = 1,
                canLoadMore = true,
                todos = emptyList(),
                blockingErrorMessage = null
            )
        }
        requestTodos(isRefresh = false)
    }

    fun loadMore() {
        val state = _uiState.value
        if (isBusy() || !state.canLoadMore) return

        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            when (val result = repository.loadTodos(state.currentPage + 1, state.selectedFilter.status)) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            currentPage = data.page,
                            canLoadMore = data.canLoadMore,
                            todos = it.todos + data.todos
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoadingMore = false) }
                    emitAuthRequiredIfNeeded(result)
                    _messages.tryEmit(result.message)
                }
            }
        }
    }

    fun addTodo(value: TodoEditorValue) {
        if (isBusy()) return
        _uiState.update { it.copy(isSubmittingEditor = true) }
        viewModelScope.launch {
            when (
                val result = repository.addTodo(
                    title = value.title,
                    content = value.content,
                    date = value.date,
                    type = value.type,
                    priority = value.priority
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSubmittingEditor = false) }
                    _messages.tryEmit("待办已创建。")
                    requestTodos(isRefresh = false)
                }

                is AppResult.Error -> {
                    _uiState.update { it.copy(isSubmittingEditor = false) }
                    emitAuthRequiredIfNeeded(result)
                    _messages.tryEmit(result.message)
                }
            }
        }
    }

    fun updateTodo(todo: TodoItem, value: TodoEditorValue) {
        if (isBusy()) return
        _uiState.update { it.copy(isSubmittingEditor = true) }
        viewModelScope.launch {
            when (
                val result = repository.updateTodo(
                    id = todo.id,
                    title = value.title,
                    content = value.content,
                    date = value.date,
                    type = value.type,
                    priority = value.priority
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSubmittingEditor = false) }
                    _messages.tryEmit("待办已更新。")
                    requestTodos(isRefresh = false)
                }

                is AppResult.Error -> {
                    _uiState.update { it.copy(isSubmittingEditor = false) }
                    emitAuthRequiredIfNeeded(result)
                    _messages.tryEmit(result.message)
                }
            }
        }
    }

    fun toggleTodoStatus(todo: TodoItem) {
        if (isBusy() || _uiState.value.actingTodoIds.contains(todo.id)) return
        _uiState.update { it.copy(actingTodoIds = it.actingTodoIds + todo.id) }
        viewModelScope.launch {
            when (val result = repository.updateTodoStatus(todo.id, if (todo.isCompleted) 0 else 1)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(actingTodoIds = it.actingTodoIds - todo.id) }
                    _messages.tryEmit(
                        if (todo.isCompleted) "待办已恢复为未完成。" else "待办已标记为完成。"
                    )
                    requestTodos(isRefresh = false)
                }

                is AppResult.Error -> {
                    _uiState.update { it.copy(actingTodoIds = it.actingTodoIds - todo.id) }
                    emitAuthRequiredIfNeeded(result)
                    _messages.tryEmit(result.message)
                }
            }
        }
    }

    fun deleteTodo(todo: TodoItem) {
        if (isBusy() || _uiState.value.actingTodoIds.contains(todo.id)) return
        _uiState.update { it.copy(actingTodoIds = it.actingTodoIds + todo.id) }
        viewModelScope.launch {
            when (val result = repository.deleteTodo(todo.id)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(actingTodoIds = it.actingTodoIds - todo.id) }
                    _messages.tryEmit("待办已删除。")
                    requestTodos(isRefresh = false)
                }

                is AppResult.Error -> {
                    _uiState.update { it.copy(actingTodoIds = it.actingTodoIds - todo.id) }
                    emitAuthRequiredIfNeeded(result)
                    _messages.tryEmit(result.message)
                }
            }
        }
    }

    private fun requestTodos(isRefresh: Boolean) {
        val filter = _uiState.value.selectedFilter
        _uiState.update {
            it.copy(
                isInitialLoading = !isRefresh,
                isRefreshing = isRefresh,
                isLoadingMore = false,
                blockingErrorMessage = null
            )
        }

        viewModelScope.launch {
            when (val result = repository.loadTodos(page = 1, status = filter.status)) {
                is AppResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            hasLoadedOnce = true,
                            blockingErrorMessage = null,
                            todos = data.todos,
                            currentPage = data.page,
                            canLoadMore = data.canLoadMore
                        )
                    }
                }

                is AppResult.Error -> {
                    val shouldShowBlocking = !_uiState.value.hasLoadedOnce
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            blockingErrorMessage = if (shouldShowBlocking) result.message else null
                        )
                    }
                    if (!shouldShowBlocking) {
                        _messages.tryEmit(result.message)
                    }
                    emitAuthRequiredIfNeeded(result)
                }
            }
        }
    }

    private fun emitAuthRequiredIfNeeded(error: AppResult.Error) {
        if (error.code == LOGIN_EXPIRED_CODE) {
            _authRequired.tryEmit(error.message)
        }
    }

    private fun isBusy(): Boolean {
        val state = _uiState.value
        return state.isInitialLoading ||
            state.isRefreshing ||
            state.isLoadingMore ||
            state.isSubmittingEditor
    }
}

class TodoViewModelFactory(
    private val repository: AccountRepository = AccountRepository(
        NetworkModule.apiService,
        NetworkModule.requireSessionStore()
    )
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
