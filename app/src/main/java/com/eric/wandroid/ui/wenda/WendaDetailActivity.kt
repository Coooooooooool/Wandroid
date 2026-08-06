package com.eric.wandroid.ui.wenda

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.ui.web.WebContainerActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class WendaDetailActivity : AppCompatActivity() {
    private val initialArticle: Article by lazy(LazyThreadSafetyMode.NONE) {
        resolveArticle() ?: error("Missing Wenda article extras.")
    }

    private val viewModel: WendaDetailViewModel by viewModels {
        WendaDetailViewModelFactory(initialArticle)
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollToTopButton: FloatingActionButton

    private val adapter = WendaDetailAdapter(
        onOpenOriginalClick = { article ->
            if (article.link.isNotBlank()) {
                startActivity(WebContainerActivity.createArticleIntent(this, toolbar.title.toString(), article.link))
            }
        },
        onCollectClick = { viewModel.toggleCollect() },
        onRetryClick = { viewModel.retry() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wenda_detail)
        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, recyclerView)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@WendaDetailActivity)
            adapter = this@WendaDetailActivity.adapter
        }
        ScrollToTopHelper.attach(recyclerView, scrollToTopButton) { progressBar.visibility == View.VISIBLE }

        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
        observeUi()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.wendaDetailRecyclerView)
        progressBar = findViewById(R.id.initialProgress)
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

    private fun render(state: WendaDetailUiState) {
        toolbar.title = getString(R.string.wenda_detail_screen_title)
        toolbar.subtitle = state.article.author.ifBlank {
            getString(R.string.discover_wenda_title)
        }
        swipeRefreshLayout.isRefreshing = state.isRefreshing
        progressBar.visibility = if (state.isInitialLoading && state.comments.isEmpty()) View.VISIBLE else View.GONE
        adapter.submitState(state)
        ScrollToTopHelper.update(recyclerView, scrollToTopButton, progressBar.visibility == View.VISIBLE)
    }

    private fun resolveArticle(): Article? {
        val id = intent.getIntExtra(EXTRA_ARTICLE_ID, Int.MIN_VALUE)
        if (id == Int.MIN_VALUE) return null
        return Article(
            id = id,
            title = intent.getStringExtra(EXTRA_ARTICLE_TITLE).orEmpty(),
            link = intent.getStringExtra(EXTRA_ARTICLE_LINK).orEmpty(),
            author = intent.getStringExtra(EXTRA_ARTICLE_AUTHOR).orEmpty(),
            userId = 0,
            shareUser = "",
            chapterName = intent.getStringExtra(EXTRA_ARTICLE_CHAPTER).orEmpty(),
            superChapterName = intent.getStringExtra(EXTRA_ARTICLE_SUPER_CHAPTER).orEmpty(),
            niceDate = intent.getStringExtra(EXTRA_ARTICLE_DATE).orEmpty(),
            desc = intent.getStringExtra(EXTRA_ARTICLE_DESC).orEmpty(),
            isCollected = intent.getBooleanExtra(EXTRA_ARTICLE_COLLECTED, false),
            isTopPinned = intent.getBooleanExtra(EXTRA_ARTICLE_TOP, false)
        )
    }

    companion object {
        private const val EXTRA_ARTICLE_ID = "extra_article_id"
        private const val EXTRA_ARTICLE_TITLE = "extra_article_title"
        private const val EXTRA_ARTICLE_LINK = "extra_article_link"
        private const val EXTRA_ARTICLE_AUTHOR = "extra_article_author"
        private const val EXTRA_ARTICLE_CHAPTER = "extra_article_chapter"
        private const val EXTRA_ARTICLE_SUPER_CHAPTER = "extra_article_super_chapter"
        private const val EXTRA_ARTICLE_DATE = "extra_article_date"
        private const val EXTRA_ARTICLE_DESC = "extra_article_desc"
        private const val EXTRA_ARTICLE_COLLECTED = "extra_article_collected"
        private const val EXTRA_ARTICLE_TOP = "extra_article_top"

        fun createIntent(context: Context, article: Article): Intent {
            return Intent(context, WendaDetailActivity::class.java)
                .putExtra(EXTRA_ARTICLE_ID, article.id)
                .putExtra(EXTRA_ARTICLE_TITLE, article.title)
                .putExtra(EXTRA_ARTICLE_LINK, article.link)
                .putExtra(EXTRA_ARTICLE_AUTHOR, article.author)
                .putExtra(EXTRA_ARTICLE_CHAPTER, article.chapterName)
                .putExtra(EXTRA_ARTICLE_SUPER_CHAPTER, article.superChapterName)
                .putExtra(EXTRA_ARTICLE_DATE, article.niceDate)
                .putExtra(EXTRA_ARTICLE_DESC, article.desc)
                .putExtra(EXTRA_ARTICLE_COLLECTED, article.isCollected)
                .putExtra(EXTRA_ARTICLE_TOP, article.isTopPinned)
        }
    }
}
