package com.eric.wandroid.ui.system

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
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
import com.eric.wandroid.domain.model.SystemChildCategory
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.eric.wandroid.common.ui.ScrollToTopHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SystemDetailActivity : AppCompatActivity() {
    private val rootId: Int by lazy(LazyThreadSafetyMode.NONE) {
        intent.getIntExtra(EXTRA_ROOT_ID, 0)
    }
    private val rootName: String by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra(EXTRA_ROOT_NAME).orEmpty()
    }

    private val viewModel: SystemDetailViewModel by viewModels {
        SystemDetailViewModelFactory(rootId = rootId, rootName = rootName)
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var fullScreenProgress: ProgressBar
    private lateinit var emptyState: View
    private lateinit var emptyTitle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var emptyAction: Button
    private lateinit var scrollToTopButton: FloatingActionButton

    private val adapter = SystemDetailAdapter(
        onCategoryClick = { viewModel.selectCategory(it) },
        onMoreCategoriesClick = { showCategoryBottomSheet() },
        onLoadMoreClick = { viewModel.loadMore() },
        onCollectClick = { viewModel.toggleCollect(it) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_detail)
        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, recyclerView)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        ScrollToTopHelper.attach(recyclerView, scrollToTopButton) { !recyclerView.isVisible }
        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
        emptyAction.setOnClickListener { viewModel.retry() }

        observeUi()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.systemDetailRecyclerView)
        fullScreenProgress = findViewById(R.id.fullScreenProgress)
        emptyState = findViewById(R.id.emptyState)
        emptyTitle = findViewById(R.id.emptyStateTitle)
        emptyMessage = findViewById(R.id.emptyStateMessage)
        emptyAction = findViewById(R.id.emptyStateAction)
        scrollToTopButton = findViewById(R.id.scrollToTopButton)
    }

    private fun observeUi() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.messages.collect { message ->
                        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
                    }
                }
                launch {
                    viewModel.authRequired.collect { message ->
                        navigateToLogin(message)
                    }
                }
            }
        }
    }

    private fun render(state: SystemDetailUiState) {
        toolbar.title = state.rootName
        toolbar.subtitle = null
        swipeRefreshLayout.isRefreshing = state.isRefreshing
        adapter.submitState(state)

        val hasContent = state.children.isNotEmpty()
        val showBlockingState = !hasContent && state.blockingErrorMessage != null
        val showEmptyState = !hasContent && !state.isInitialLoading && state.blockingErrorMessage == null

        recyclerView.isVisible = hasContent
        fullScreenProgress.isVisible =
            (state.isInitialLoading && !hasContent) ||
                (state.isCategoryLoading && state.articles.isEmpty() && hasContent)
        emptyState.isVisible = showBlockingState || showEmptyState
        ScrollToTopHelper.update(recyclerView, scrollToTopButton, !hasContent)

        if (showBlockingState) {
            emptyTitle.setText(R.string.error_title)
            emptyMessage.text = state.blockingErrorMessage
            emptyAction.setText(R.string.label_retry)
        } else {
            emptyTitle.setText(R.string.empty_title)
            emptyMessage.setText(R.string.system_detail_empty_message)
            emptyAction.setText(R.string.label_refresh)
        }
    }

    private fun showCategoryBottomSheet() {
        val categories = viewModel.uiState.value.children
        if (categories.isEmpty()) return
        val dialog = BottomSheetDialog(this)
        val sheetView = LayoutInflater.from(this)
            .inflate(R.layout.bottom_sheet_system_categories, null, false)
        val titleView = sheetView.findViewById<TextView>(R.id.bottomSheetTitle)
        val recyclerView = sheetView.findViewById<RecyclerView>(R.id.categoryRecyclerView)

        titleView.setText(R.string.system_detail_more_categories)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CategoryBottomSheetAdapter(
            categories = categories,
            selectedCategoryId = viewModel.uiState.value.selectedCategoryId,
            onCategoryClick = { category ->
                viewModel.selectCategory(category)
                dialog.dismiss()
            }
        )

        dialog.setContentView(sheetView)
        dialog.show()
    }

    private class CategoryBottomSheetAdapter(
        private val categories: List<SystemChildCategory>,
        private val selectedCategoryId: Int?,
        private val onCategoryClick: (SystemChildCategory) -> Unit
    ) : RecyclerView.Adapter<CategoryBottomSheetAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_system_category_bottom_sheet, parent, false)
            return ViewHolder(view, onCategoryClick)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(categories[position], categories[position].id == selectedCategoryId)
        }

        override fun getItemCount(): Int = categories.size

        class ViewHolder(
            itemView: View,
            private val onCategoryClick: (SystemChildCategory) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {
            private val nameView: TextView = itemView.findViewById(R.id.categoryName)
            private val statusView: TextView = itemView.findViewById(R.id.categoryStatus)

            fun bind(category: SystemChildCategory, isSelected: Boolean) {
                nameView.text = category.name
                statusView.isVisible = isSelected
                statusView.setText(R.string.system_detail_current_category)
                itemView.setOnClickListener { onCategoryClick(category) }
            }
        }
    }

    companion object {
        private const val EXTRA_ROOT_ID = "system_root_id"
        private const val EXTRA_ROOT_NAME = "system_root_name"

        fun createIntent(context: Context, rootId: Int, rootName: String): Intent {
            return Intent(context, SystemDetailActivity::class.java)
                .putExtra(EXTRA_ROOT_ID, rootId)
                .putExtra(EXTRA_ROOT_NAME, rootName)
        }
    }
}
