package com.eric.wandroid.ui.moyu.internet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.eric.wandroid.common.ui.ScrollToTopHelper
import com.eric.wandroid.ui.web.WebContainerActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class InternetNewsActivity : AppCompatActivity() {
    private val viewModel: InternetNewsViewModel by viewModels { InternetNewsViewModelFactory() }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: View
    private lateinit var emptyTitle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var emptyAction: Button
    private lateinit var scrollToTopButton: FloatingActionButton

    private val adapter = InternetNewsAdapter(
        onSearchSubmit = { viewModel.submitKeyword(it) },
        onArticleClick = {
            startActivity(WebContainerActivity.createIntent(this, it.title, it.url))
        },
        onLoadMoreClick = { viewModel.loadMore() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internet_news)
        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, recyclerView)
        setSupportActionBar(toolbar)
        setTitle(R.string.internet_news_screen_title)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setTitle(R.string.internet_news_screen_title)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        ScrollToTopHelper.attach(recyclerView, scrollToTopButton) { recyclerView.visibility != View.VISIBLE }
        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
        emptyAction.setOnClickListener { viewModel.retry() }

        observeUi()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.internetNewsRecyclerView)
        progressBar = findViewById(R.id.fullScreenProgress)
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
            }
        }
    }

    private fun render(state: InternetNewsUiState) {
        toolbar.subtitle = state.keyword
        swipeRefreshLayout.isRefreshing = state.isRefreshing
        recyclerView.visibility = if (state.hasContent || state.isInitialLoading) View.VISIBLE else View.GONE
        progressBar.visibility = if (state.isInitialLoading && !state.hasContent) View.VISIBLE else View.GONE
        adapter.submitState(state)
        ScrollToTopHelper.update(recyclerView, scrollToTopButton, !state.hasContent)

        val showEmpty = !state.hasContent && !state.isInitialLoading
        emptyState.visibility = if (showEmpty) View.VISIBLE else View.GONE
        if (showEmpty) {
            if (state.blockingErrorMessage != null) {
                emptyTitle.setText(R.string.error_title)
                emptyMessage.text = state.blockingErrorMessage
                emptyAction.setText(R.string.label_retry)
            } else {
                emptyTitle.setText(R.string.empty_title)
                emptyMessage.setText(R.string.internet_news_empty_message)
                emptyAction.setText(R.string.label_refresh)
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, InternetNewsActivity::class.java)
        }
    }
}
