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
import com.eric.wandroid.common.auth.navigateToLogin
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.eric.wandroid.common.ui.ScrollToTopHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SystemNavigationActivity : AppCompatActivity() {
    private val initialMode: SystemContentMode by lazy(LazyThreadSafetyMode.NONE) {
        resolveInitialMode() ?: SystemContentMode.System
    }

    private val viewModel: SystemViewModel by viewModels {
        SystemViewModelFactory(initialMode = initialMode)
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

    private val adapter = SystemAdapter(
        onCategoryClick = { viewModel.selectCategory(it) },
        onLoadMoreClick = { viewModel.loadMore() },
        onCollectClick = { viewModel.toggleCollect(it) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_navigation)
        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, recyclerView)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SystemNavigationActivity)
            adapter = this@SystemNavigationActivity.adapter
        }
        ScrollToTopHelper.attach(recyclerView, scrollToTopButton) { !recyclerView.isVisible }

        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
        emptyAction.setOnClickListener { viewModel.retry() }

        observeUi()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.systemRecyclerView)
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

    private fun resolveInitialMode(): SystemContentMode? {
        val rawMode = intent.getStringExtra(EXTRA_INITIAL_MODE) ?: return null
        return SystemContentMode.entries.firstOrNull { it.name == rawMode }
    }

    private fun render(state: SystemUiState) {
        toolbar.title = getString(
            when (state.mode) {
                SystemContentMode.System -> R.string.discover_system_only_title
                SystemContentMode.Navigation -> R.string.discover_navigation_title
            }
        )
        toolbar.subtitle = null

        swipeRefreshLayout.isRefreshing = state.isRefreshing
        adapter.submitState(state)

        val hasPrimaryContent = state.hasOverview
        val showBlockingState = !hasPrimaryContent && state.blockingErrorMessage != null
        val showEmptyState = !hasPrimaryContent && !state.isInitialLoading && state.blockingErrorMessage == null

        recyclerView.isVisible = hasPrimaryContent
        fullScreenProgress.isVisible =
            (state.isInitialLoading && !hasPrimaryContent) ||
                (state.mode == SystemContentMode.System &&
                    state.isCategoryLoading &&
                    state.articles.isEmpty() &&
                    hasPrimaryContent)
        emptyState.isVisible = showBlockingState || showEmptyState
        ScrollToTopHelper.update(recyclerView, scrollToTopButton, !hasPrimaryContent)

        if (showBlockingState) {
            emptyTitle.setText(R.string.error_title)
            emptyMessage.text = state.blockingErrorMessage
            emptyAction.setText(R.string.label_retry)
        } else {
            emptyTitle.setText(R.string.empty_title)
            emptyMessage.setText(
                when (state.mode) {
                    SystemContentMode.System -> R.string.system_empty_message
                    SystemContentMode.Navigation -> R.string.navigation_empty_message
                }
            )
            emptyAction.setText(R.string.label_refresh)
        }
    }

    companion object {
        private const val EXTRA_INITIAL_MODE = "system_initial_mode"

        fun createIntent(context: Context, initialMode: SystemContentMode): Intent {
            return Intent(context, SystemNavigationActivity::class.java)
                .putExtra(EXTRA_INITIAL_MODE, initialMode.name)
        }
    }
}
