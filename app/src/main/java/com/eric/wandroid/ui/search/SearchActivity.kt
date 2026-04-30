package com.eric.wandroid.ui.search

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
import com.eric.wandroid.common.auth.navigateToLogin
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.eric.wandroid.common.ui.ScrollToTopHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    private val viewModel: SearchViewModel by viewModels { SearchViewModelFactory() }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var fullScreenProgress: ProgressBar
    private lateinit var emptyState: View
    private lateinit var emptyTitle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var emptyAction: Button
    private lateinit var scrollToTopButton: FloatingActionButton

    private val adapter = SearchAdapter(
        onSearchSubmit = { viewModel.submitSearch(it) },
        onHotKeyClick = { viewModel.selectHotKey(it) },
        onSearchHistoryClick = { viewModel.submitSearch(it) },
        onSearchHistoryLongClick = {
            viewModel.removeSearchHistory(it)
            Snackbar.make(findViewById(android.R.id.content), R.string.search_history_removed, Snackbar.LENGTH_SHORT).show()
        },
        onClearSearchHistory = {
            viewModel.clearSearchHistory()
            Snackbar.make(findViewById(android.R.id.content), R.string.search_history_cleared, Snackbar.LENGTH_SHORT).show()
        },
        onClearSearch = { viewModel.clearSearch() },
        onLoadMoreClick = { viewModel.loadMore() },
        onCollectClick = { viewModel.toggleCollect(it) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, recyclerView)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = this@SearchActivity.adapter
        }
        ScrollToTopHelper.attach(recyclerView, scrollToTopButton) { !recyclerView.isVisible }

        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
        emptyAction.setOnClickListener { viewModel.retry() }

        observeUi()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.searchRecyclerView)
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

    private fun render(state: SearchUiState) {
        toolbar.subtitle = if (state.isResultMode) {
            getString(R.string.project_search_mode_subtitle, state.keyword)
        } else {
            getString(R.string.search_screen_subtitle)
        }

        swipeRefreshLayout.isRefreshing = state.isRefreshing
        adapter.submitState(state)

        val showBlockingState = !state.isResultMode &&
            state.hotKeys.isEmpty() &&
            state.searchHistory.isEmpty() &&
            state.blockingErrorMessage != null

        recyclerView.isVisible = !showBlockingState
        fullScreenProgress.isVisible =
            state.isInitialLoading && !state.isResultMode && state.hotKeys.isEmpty() && state.searchHistory.isEmpty()
        emptyState.isVisible = showBlockingState
        ScrollToTopHelper.update(recyclerView, scrollToTopButton, showBlockingState)

        if (showBlockingState) {
            emptyTitle.setText(R.string.error_title)
            emptyMessage.text = state.blockingErrorMessage
            emptyAction.setText(R.string.label_retry)
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, SearchActivity::class.java)
        }
    }
}
