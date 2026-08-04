package com.eric.wandroid.ui.todo

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.eric.wandroid.R
import com.eric.wandroid.common.auth.navigateToLogin
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.eric.wandroid.domain.model.TodoItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.ChipGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TodoActivity : AppCompatActivity() {
    private val viewModel: TodoViewModel by viewModels { TodoViewModelFactory() }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var fullScreenProgress: ProgressBar
    private lateinit var emptyState: View
    private lateinit var emptyTitle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var emptyAction: Button

    private val adapter = TodoAdapter(
        onFilterSelected = { viewModel.selectFilter(it) },
        onEditClick = { showEditor(todo = it) },
        onToggleStatusClick = { viewModel.toggleTodoStatus(it) },
        onDeleteClick = { confirmDelete(it) },
        onLoadMoreClick = { viewModel.loadMore() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)
        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, recyclerView)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TodoActivity)
            adapter = this@TodoActivity.adapter
        }

        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
        emptyAction.setOnClickListener { viewModel.retry() }

        observeUi()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.todoRecyclerView)
        fullScreenProgress = findViewById(R.id.fullScreenProgress)
        emptyState = findViewById(R.id.emptyState)
        emptyTitle = findViewById(R.id.emptyStateTitle)
        emptyMessage = findViewById(R.id.emptyStateMessage)
        emptyAction = findViewById(R.id.emptyStateAction)
    }

    private fun observeUi() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.messages.collect { message ->
                        val text = when (message) {
                            is TodoMessage.Text -> message.value
                            is TodoMessage.Resource -> getString(message.id)
                        }
                        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show()
                    }
                }
                launch {
                    viewModel.authRequired.collect { message ->
                        navigateToLogin(message, finishCurrent = true)
                    }
                }
            }
        }
    }

    private fun render(state: TodoUiState) {
        toolbar.subtitle = null
        swipeRefreshLayout.isRefreshing = state.isRefreshing
        adapter.submitState(state)
        toolbar.menu.findItem(R.id.action_add_todo)?.isEnabled = !state.isSubmittingEditor

        val showBlockingState = !state.hasLoadedOnce && state.blockingErrorMessage != null
        val showListContent = state.hasLoadedOnce || state.hasContent

        recyclerView.isVisible = showListContent
        fullScreenProgress.isVisible = state.isInitialLoading && !state.hasLoadedOnce
        emptyState.isVisible = showBlockingState

        if (showBlockingState) {
            emptyTitle.setText(R.string.error_title)
            emptyMessage.text = state.blockingErrorMessage
            emptyAction.setText(R.string.label_retry)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_todo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_add_todo) {
            showEditor(todo = null)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun showEditor(todo: TodoItem?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_todo_editor, null, false)
        val sheetTitle = dialogView.findViewById<TextView>(R.id.todoSheetTitle)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.todoCancelButton)
        val submitButton = dialogView.findViewById<MaterialButton>(R.id.todoSubmitButton)
        val titleLayout = dialogView.findViewById<TextInputLayout>(R.id.todoTitleLayout)
        val contentLayout = dialogView.findViewById<TextInputLayout>(R.id.todoContentLayout)
        val dateLayout = dialogView.findViewById<TextInputLayout>(R.id.todoDateLayout)
        val titleInput = dialogView.findViewById<TextInputEditText>(R.id.todoTitleInput)
        val contentInput = dialogView.findViewById<TextInputEditText>(R.id.todoContentInput)
        val dateInput = dialogView.findViewById<TextInputEditText>(R.id.todoDateInput)
        val typeGroup = dialogView.findViewById<ChipGroup>(R.id.todoTypeGroup)
        val priorityGroup = dialogView.findViewById<ChipGroup>(R.id.todoPriorityGroup)

        titleInput.setText(todo?.title.orEmpty())
        contentInput.setText(todo?.content.orEmpty())
        dateInput.setText(todo?.dateText.orEmpty().ifBlank { todayString() })
        setCheckedTypeChip(typeGroup, todo?.type ?: 3)
        setCheckedPriorityChip(priorityGroup, todo?.priority ?: 2)
        sheetTitle.setText(if (todo == null) R.string.todo_add_title else R.string.todo_edit_title)
        submitButton.setText(if (todo == null) R.string.todo_save_action else R.string.todo_update_action)

        dateInput.apply {
            keyListener = null
            isFocusable = false
            setOnClickListener {
                showDatePicker(currentValue = text?.toString().orEmpty()) { selectedDate ->
                    setText(selectedDate)
                }
            }
        }

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)
        cancelButton.setOnClickListener { dialog.dismiss() }
        submitButton.setOnClickListener {
            titleLayout.error = null
            contentLayout.error = null
            dateLayout.error = null
            val value = buildEditorValue(
                title = titleInput.text?.toString().orEmpty(),
                content = contentInput.text?.toString().orEmpty(),
                date = dateInput.text?.toString().orEmpty(),
                titleLayout = titleLayout,
                contentLayout = contentLayout,
                dateLayout = dateLayout,
                typeGroup = typeGroup,
                priorityGroup = priorityGroup
            ) ?: return@setOnClickListener

            if (todo == null) {
                viewModel.addTodo(value)
            } else {
                viewModel.updateTodo(todo, value)
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun buildEditorValue(
        title: String,
        content: String,
        date: String,
        titleLayout: TextInputLayout,
        contentLayout: TextInputLayout,
        dateLayout: TextInputLayout,
        typeGroup: ChipGroup,
        priorityGroup: ChipGroup
    ): TodoEditorValue? {
        if (title.isBlank()) {
            titleLayout.error = getString(R.string.todo_title_required)
            return null
        }
        if (content.isBlank()) {
            contentLayout.error = getString(R.string.todo_content_required)
            return null
        }
        if (date.isBlank()) {
            dateLayout.error = getString(R.string.todo_date_required)
            return null
        }

        return TodoEditorValue(
            title = title.trim(),
            content = content.trim(),
            date = date.trim(),
            type = when (typeGroup.checkedChipId) {
                R.id.todoTypeWorkChip -> 1
                R.id.todoTypeLifeChip -> 2
                R.id.todoTypeAllChip -> 4
                else -> 3
            },
            priority = if (priorityGroup.checkedChipId == R.id.todoPriorityHighChip) 1 else 2
        )
    }

    private fun confirmDelete(todo: TodoItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.todo_delete_title)
            .setMessage(getString(R.string.todo_delete_message, todo.title))
            .setNegativeButton(R.string.todo_cancel_action, null)
            .setPositiveButton(R.string.todo_delete_action) { _, _ ->
                viewModel.deleteTodo(todo)
            }
            .show()
    }

    private fun showDatePicker(currentValue: String, onDateSelected: (String) -> Unit) {
        val calendar = parseDate(currentValue)
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                onDateSelected(formatDate(selected))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun parseDate(value: String): Calendar {
        val calendar = Calendar.getInstance()
        runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value)
        }.getOrNull()?.let { parsed ->
            calendar.time = parsed
        }
        return calendar
    }

    private fun formatDate(calendar: Calendar): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    private fun todayString(): String = formatDate(Calendar.getInstance())

    private fun setCheckedTypeChip(group: ChipGroup, type: Int) {
        group.check(
            when (type) {
                1 -> R.id.todoTypeWorkChip
                2 -> R.id.todoTypeLifeChip
                4 -> R.id.todoTypeAllChip
                else -> R.id.todoTypeMixedChip
            }
        )
    }

    private fun setCheckedPriorityChip(group: ChipGroup, priority: Int) {
        group.check(
            if (priority == 1) {
                R.id.todoPriorityHighChip
            } else {
                R.id.todoPriorityNormalChip
            }
        )
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, TodoActivity::class.java)
        }
    }
}
