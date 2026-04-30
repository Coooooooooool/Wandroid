package com.eric.wandroid.ui.system

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.eric.wandroid.common.ui.ScrollToTopHelper
import kotlinx.coroutines.launch
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SystemRootListActivity : AppCompatActivity() {
    private val viewModel: SystemRootListViewModel by viewModels { SystemRootListViewModelFactory() }

    private lateinit var toolbar: com.google.android.material.appbar.MaterialToolbar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var fullScreenProgress: ProgressBar
    private lateinit var emptyState: View
    private lateinit var emptyTitle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var emptyAction: Button
    private lateinit var scrollToTopButton: FloatingActionButton

    private val adapter = SystemRootListAdapter { root ->
        startActivity(SystemDetailActivity.createIntent(this, root.id, root.name))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_root_list)
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
        recyclerView = findViewById(R.id.systemRootRecyclerView)
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
            }
        }
    }

    private fun render(state: SystemRootListUiState) {
        swipeRefreshLayout.isRefreshing = state.isRefreshing
        adapter.submitList(state.roots)

        val hasContent = state.roots.isNotEmpty()
        val showBlockingState = !hasContent && state.blockingErrorMessage != null
        val showEmptyState = !hasContent && !state.isInitialLoading && state.blockingErrorMessage == null

        recyclerView.isVisible = hasContent
        fullScreenProgress.isVisible = state.isInitialLoading && !hasContent
        emptyState.isVisible = showBlockingState || showEmptyState
        ScrollToTopHelper.update(recyclerView, scrollToTopButton, !hasContent)

        if (showBlockingState) {
            emptyTitle.setText(R.string.error_title)
            emptyMessage.text = state.blockingErrorMessage
            emptyAction.setText(R.string.label_retry)
        } else {
            emptyTitle.setText(R.string.empty_title)
            emptyMessage.setText(R.string.system_root_empty_message)
            emptyAction.setText(R.string.label_refresh)
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, SystemRootListActivity::class.java)
        }
    }
}
