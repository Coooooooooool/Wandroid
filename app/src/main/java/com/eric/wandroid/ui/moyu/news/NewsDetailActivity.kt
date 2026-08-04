package com.eric.wandroid.ui.moyu.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
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
import com.eric.wandroid.common.ui.RemoteImageLoader
import com.eric.wandroid.common.ui.ScrollToTopHelper
import com.eric.wandroid.domain.model.NewsArticle
import com.eric.wandroid.ui.web.WebContainerActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class NewsDetailActivity : AppCompatActivity() {
    private val initialArticle: NewsArticle by lazy(LazyThreadSafetyMode.NONE) {
        resolveArticle() ?: error("Missing news article extras.")
    }

    private val viewModel: NewsDetailViewModel by viewModels {
        NewsDetailViewModelFactory(initialArticle)
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: View
    private lateinit var emptyTitle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var emptyAction: Button
    private lateinit var headerImage: ImageView
    private lateinit var titleView: TextView
    private lateinit var metaView: TextView
    private lateinit var sourceButton: MaterialButton
    private lateinit var scrollToTopButton: FloatingActionButton

    private val adapter = NewsContentAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)
        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, swipeRefreshLayout)
        setSupportActionBar(toolbar)
        setTitle(R.string.news_detail_title)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        ScrollToTopHelper.attach(recyclerView, scrollToTopButton) { recyclerView.visibility != View.VISIBLE }
        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
        emptyAction.setOnClickListener { viewModel.retry() }
        sourceButton.setOnClickListener {
            val detail = viewModel.uiState.value.detail ?: return@setOnClickListener
            if (detail.article.url.isNotBlank()) {
                startActivity(WebContainerActivity.createIntent(this, detail.article.title, detail.article.url))
            }
        }

        observeUi()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.newsContentRecyclerView)
        progressBar = findViewById(R.id.fullScreenProgress)
        emptyState = findViewById(R.id.emptyState)
        emptyTitle = findViewById(R.id.emptyStateTitle)
        emptyMessage = findViewById(R.id.emptyStateMessage)
        emptyAction = findViewById(R.id.emptyStateAction)
        headerImage = findViewById(R.id.newsHeaderImage)
        titleView = findViewById(R.id.newsDetailTitle)
        metaView = findViewById(R.id.newsDetailMeta)
        sourceButton = findViewById(R.id.newsSourceButton)
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

    private fun render(state: NewsDetailUiState) {
        val article = state.detail?.article ?: state.article
        setTitle(R.string.news_detail_title)
        toolbar.setTitle(R.string.news_detail_title)
        toolbar.subtitle = article.category.ifBlank { null }
        swipeRefreshLayout.isRefreshing = state.isRefreshing

        val headerImageUrl = article.previewImageUrl
        headerImage.isVisible = headerImageUrl.isNotBlank()
        if (headerImageUrl.isNotBlank()) {
            RemoteImageLoader.loadInto(headerImage, headerImageUrl)
        }
        titleView.text = article.title
        metaView.text = buildList {
            if (article.authorName.isNotBlank()) add(article.authorName)
            if (article.date.isNotBlank()) add(article.date)
            if (article.category.isNotBlank()) add(article.category)
        }.joinToString(" · ")
        sourceButton.isVisible = article.url.isNotBlank()

        val blocks = state.detail?.let { NewsContentParser.parse(it.contentHtml) }.orEmpty()
        adapter.submitItems(blocks)
        recyclerView.visibility = if (state.hasContent) View.VISIBLE else View.GONE
        progressBar.visibility = if (state.isInitialLoading && !state.hasContent) View.VISIBLE else View.GONE
        ScrollToTopHelper.update(recyclerView, scrollToTopButton, !state.hasContent)

        val showEmpty = !state.hasContent && !state.isInitialLoading
        emptyState.visibility = if (showEmpty) View.VISIBLE else View.GONE
        if (showEmpty) {
            emptyTitle.setText(R.string.error_title)
            emptyMessage.text = state.blockingErrorMessage
                ?: getString(R.string.news_detail_empty_message)
            emptyAction.setText(R.string.label_retry)
        }
    }

    private fun resolveArticle(): NewsArticle? {
        val uniqueKey = intent.getStringExtra(EXTRA_UNIQUE_KEY).orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        if (uniqueKey.isBlank() || title.isBlank()) return null
        return NewsArticle(
            uniqueKey = uniqueKey,
            title = title,
            date = intent.getStringExtra(EXTRA_DATE).orEmpty(),
            category = intent.getStringExtra(EXTRA_CATEGORY).orEmpty(),
            authorName = intent.getStringExtra(EXTRA_AUTHOR).orEmpty(),
            url = intent.getStringExtra(EXTRA_URL).orEmpty(),
            thumbnailPicS = intent.getStringExtra(EXTRA_THUMB_1).orEmpty(),
            thumbnailPicS02 = intent.getStringExtra(EXTRA_THUMB_2).orEmpty(),
            thumbnailPicS03 = intent.getStringExtra(EXTRA_THUMB_3).orEmpty(),
            hasContent = intent.getBooleanExtra(EXTRA_HAS_CONTENT, false)
        )
    }

    companion object {
        private const val EXTRA_UNIQUE_KEY = "extra_unique_key"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_DATE = "extra_date"
        private const val EXTRA_CATEGORY = "extra_category"
        private const val EXTRA_AUTHOR = "extra_author"
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_THUMB_1 = "extra_thumb_1"
        private const val EXTRA_THUMB_2 = "extra_thumb_2"
        private const val EXTRA_THUMB_3 = "extra_thumb_3"
        private const val EXTRA_HAS_CONTENT = "extra_has_content"

        fun createIntent(context: Context, article: NewsArticle): Intent {
            return Intent(context, NewsDetailActivity::class.java)
                .putExtra(EXTRA_UNIQUE_KEY, article.uniqueKey)
                .putExtra(EXTRA_TITLE, article.title)
                .putExtra(EXTRA_DATE, article.date)
                .putExtra(EXTRA_CATEGORY, article.category)
                .putExtra(EXTRA_AUTHOR, article.authorName)
                .putExtra(EXTRA_URL, article.url)
                .putExtra(EXTRA_THUMB_1, article.thumbnailPicS)
                .putExtra(EXTRA_THUMB_2, article.thumbnailPicS02)
                .putExtra(EXTRA_THUMB_3, article.thumbnailPicS03)
                .putExtra(EXTRA_HAS_CONTENT, article.hasContent)
        }
    }
}
