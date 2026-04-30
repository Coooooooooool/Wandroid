package com.eric.wandroid.ui.todo

enum class TodoFilter(val status: Int?) {
    All(status = null),
    Pending(status = 0),
    Completed(status = 1)
}
