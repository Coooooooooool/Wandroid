package com.eric.wandroid.ui.todo

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.domain.model.TodoItem
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class TodoAdapter(
    private val onFilterSelected: (TodoFilter) -> Unit,
    private val onEditClick: (TodoItem) -> Unit,
    private val onToggleStatusClick: (TodoItem) -> Unit,
    private val onDeleteClick: (TodoItem) -> Unit,
    private val onLoadMoreClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<TodoListItem>()
    private var actingTodoIds: Set<Int> = emptySet()

    fun submitState(state: TodoUiState) {
        items.clear()
        actingTodoIds = state.actingTodoIds
        items += TodoListItem.FilterSection(state.selectedFilter)
        items += TodoListItem.Header(state.todos.size)
        if (state.todos.isEmpty()) {
            items += TodoListItem.EmptyHint(state.selectedFilter)
        } else {
            items += state.todos.map { TodoListItem.TodoRow(it) }
            items += TodoListItem.LoadMoreFooter(state.isLoadingMore, state.canLoadMore)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is TodoListItem.FilterSection -> VIEW_TYPE_FILTER
        is TodoListItem.Header -> VIEW_TYPE_HEADER
        is TodoListItem.EmptyHint -> VIEW_TYPE_EMPTY
        is TodoListItem.TodoRow -> VIEW_TYPE_TODO
        is TodoListItem.LoadMoreFooter -> VIEW_TYPE_LOAD_MORE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_FILTER -> FilterViewHolder(
                inflater.inflate(R.layout.item_extra_chip_section, parent, false),
                onFilterSelected
            )
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_system_article_header, parent, false)
            )
            VIEW_TYPE_EMPTY -> EmptyHintViewHolder(
                inflater.inflate(R.layout.item_todo_empty_hint, parent, false)
            )
            VIEW_TYPE_TODO -> TodoViewHolder(
                inflater.inflate(R.layout.item_todo_row, parent, false),
                onEditClick,
                onToggleStatusClick,
                onDeleteClick
            )
            VIEW_TYPE_LOAD_MORE -> LoadMoreViewHolder(
                inflater.inflate(R.layout.item_home_load_more, parent, false),
                onLoadMoreClick
            )
            else -> error("Unsupported view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TodoListItem.FilterSection -> (holder as FilterViewHolder).bind(item.selectedFilter)
            is TodoListItem.Header -> (holder as HeaderViewHolder).bind(item.count)
            is TodoListItem.EmptyHint -> (holder as EmptyHintViewHolder).bind(item.selectedFilter)
            is TodoListItem.TodoRow -> (holder as TodoViewHolder).bind(
                item.todo,
                actingTodoIds.contains(item.todo.id)
            )
            is TodoListItem.LoadMoreFooter -> (holder as LoadMoreViewHolder).bind(item)
        }
    }

    private class FilterViewHolder(
        itemView: View,
        private val onFilterSelected: (TodoFilter) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.sectionTitle)
        private val chipGroup: ChipGroup = itemView.findViewById(R.id.chipGroup)

        fun bind(selectedFilter: TodoFilter) {
            titleView.setText(R.string.todo_filter_title)
            chipGroup.removeAllViews()
            TodoFilter.entries.forEach { filter ->
                val chip = Chip(itemView.context).apply {
                    text = itemView.context.getString(
                        when (filter) {
                            TodoFilter.All -> R.string.todo_filter_all
                            TodoFilter.Pending -> R.string.todo_filter_pending
                            TodoFilter.Completed -> R.string.todo_filter_completed
                        }
                    )
                    isCheckable = true
                    isChecked = filter == selectedFilter
                    checkedIcon = null
                    chipBackgroundColor = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            itemView.context,
                            if (isChecked) R.color.brand_secondary else R.color.surface_tint
                        )
                    )
                    setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            if (isChecked) R.color.white else R.color.text_primary
                        )
                    )
                    setOnClickListener { onFilterSelected(filter) }
                }
                chipGroup.addView(chip)
            }
        }
    }

    private class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.systemArticleTitle)
        private val subtitleView: TextView = itemView.findViewById(R.id.systemArticleSubtitle)

        fun bind(count: Int) {
            titleView.setText(R.string.todo_list_title)
            subtitleView.text = itemView.context.getString(R.string.article_section_count, count)
        }
    }

    private class EmptyHintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.emptyHintTitle)
        private val messageView: TextView = itemView.findViewById(R.id.emptyHintMessage)

        fun bind(selectedFilter: TodoFilter) {
            titleView.setText(R.string.empty_title)
            messageView.text = itemView.context.getString(
                when (selectedFilter) {
                    TodoFilter.All -> R.string.todo_empty_all_inline_message
                    TodoFilter.Pending -> R.string.todo_empty_pending_inline_message
                    TodoFilter.Completed -> R.string.todo_empty_completed_inline_message
                }
            )
        }
    }

    private class TodoViewHolder(
        itemView: View,
        private val onEditClick: (TodoItem) -> Unit,
        private val onToggleStatusClick: (TodoItem) -> Unit,
        private val onDeleteClick: (TodoItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val statusView: TextView = itemView.findViewById(R.id.todoStatus)
        private val titleView: TextView = itemView.findViewById(R.id.todoTitle)
        private val contentView: TextView = itemView.findViewById(R.id.todoContent)
        private val metaView: TextView = itemView.findViewById(R.id.todoMeta)
        private val editButton: Button = itemView.findViewById(R.id.todoEditButton)
        private val toggleButton: Button = itemView.findViewById(R.id.todoToggleButton)
        private val deleteButton: Button = itemView.findViewById(R.id.todoDeleteButton)

        fun bind(todo: TodoItem, isActing: Boolean) {
            statusView.text = itemView.context.getString(
                if (todo.isCompleted) R.string.todo_status_completed else R.string.todo_status_pending
            )
            statusView.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (todo.isCompleted) R.color.success else R.color.brand_secondary
                )
            )
            titleView.text = todo.title.ifBlank { itemView.context.getString(R.string.todo_title_fallback) }
            contentView.text = todo.content.ifBlank { itemView.context.getString(R.string.todo_content_fallback) }
            metaView.text = buildString {
                append(itemView.context.getString(R.string.todo_type_label, resolveTypeLabel(todo.type)))
                append("  |  ")
                append(itemView.context.getString(R.string.todo_priority_label, resolvePriorityLabel(todo.priority)))
                append("  |  ")
                append(itemView.context.getString(R.string.todo_date_label, todo.dateText.ifBlank { "-" }))
                if (todo.isCompleted && todo.completeDateText.isNotBlank()) {
                    append("  |  ")
                    append(itemView.context.getString(R.string.todo_complete_date_label, todo.completeDateText))
                }
            }

            editButton.isEnabled = !isActing
            toggleButton.isEnabled = !isActing
            deleteButton.isEnabled = !isActing
            toggleButton.text = itemView.context.getString(
                if (todo.isCompleted) R.string.todo_restore_action else R.string.todo_done_action
            )

            editButton.setOnClickListener { onEditClick(todo) }
            toggleButton.setOnClickListener { onToggleStatusClick(todo) }
            deleteButton.setOnClickListener { onDeleteClick(todo) }
        }

        private fun resolveTypeLabel(type: Int): String {
            return itemView.context.getString(
                when (type) {
                    1 -> R.string.todo_type_work
                    2 -> R.string.todo_type_life
                    4 -> R.string.todo_type_all
                    else -> R.string.todo_type_mixed
                }
            )
        }

        private fun resolvePriorityLabel(priority: Int): String {
            return itemView.context.getString(
                if (priority == 1) R.string.todo_priority_high else R.string.todo_priority_normal
            )
        }
    }

    private class LoadMoreViewHolder(
        itemView: View,
        onLoadMoreClick: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val button: Button = itemView.findViewById(R.id.loadMoreButton)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.loadMoreProgress)

        init {
            button.setOnClickListener { onLoadMoreClick() }
        }

        fun bind(item: TodoListItem.LoadMoreFooter) {
            progressBar.isVisible = item.isLoading
            button.isEnabled = item.canLoadMore && !item.isLoading
            button.text = when {
                item.isLoading -> itemView.context.getString(R.string.collect_action_loading)
                item.canLoadMore -> itemView.context.getString(R.string.label_load_more)
                else -> itemView.context.getString(R.string.label_no_more)
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_FILTER = 1
        private const val VIEW_TYPE_HEADER = 2
        private const val VIEW_TYPE_EMPTY = 3
        private const val VIEW_TYPE_TODO = 4
        private const val VIEW_TYPE_LOAD_MORE = 5
    }
}
