package com.eric.wandroid.ui.web

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.eric.wandroid.data.history.HistoryRepository
import com.google.android.material.appbar.MaterialToolbar

class WebContainerActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: View
    private lateinit var emptyTitle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var retryButton: Button

    private var pageTitle: String = ""
    private var pageUrl: String = ""
    private var isShareableArticle: Boolean = false
    private var hasRecordedHistory: Boolean = false
    private val historyRepository by lazy(LazyThreadSafetyMode.NONE) {
        HistoryRepository.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_container)
        pageTitle = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        pageUrl = intent.getStringExtra(EXTRA_URL).orEmpty()
        isShareableArticle = intent.getBooleanExtra(EXTRA_SHAREABLE_ARTICLE, false)

        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, webView, emptyState)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { handleBackPress() }
        retryButton.setOnClickListener { loadPage(pageUrl) }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })

        configureWebView()
        renderToolbarTitle(pageTitle)
        if (pageUrl.isBlank()) {
            showError(message = getString(R.string.web_invalid_url))
        } else {
            loadPage(pageUrl)
        }
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.webProgress)
        emptyState = findViewById(R.id.emptyState)
        emptyTitle = findViewById(R.id.emptyStateTitle)
        emptyMessage = findViewById(R.id.emptyStateMessage)
        retryButton = findViewById(R.id.emptyStateAction)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isShareableArticle && pageUrl.isNotBlank()) {
            menuInflater.inflate(R.menu.menu_web_article, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share_article -> {
                shareArticle()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun configureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.isVisible = newProgress in 0..99
                progressBar.progress = newProgress
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                renderToolbarTitle(title.orEmpty().ifBlank { pageTitle })
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.isVisible = true
                emptyState.isVisible = false
                webView.isVisible = true
                pageUrl = url.orEmpty().ifBlank { pageUrl }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.isVisible = false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    showError(
                        message = error?.description?.toString().orEmpty()
                            .ifBlank { getString(R.string.request_failed) }
                    )
                }
            }
        }
    }

    private fun loadPage(url: String) {
        if (url.isBlank()) {
            showError(message = getString(R.string.web_invalid_url))
            return
        }
        if (!hasRecordedHistory) {
            historyRepository.saveReadingHistory(pageTitle, url)
            hasRecordedHistory = true
        }
        progressBar.isVisible = true
        emptyState.isVisible = false
        webView.isVisible = true
        webView.loadUrl(url)
    }

    private fun showError(message: String) {
        progressBar.isVisible = false
        webView.isVisible = false
        emptyState.isVisible = true
        emptyTitle.setText(R.string.error_title)
        emptyMessage.text = message
        retryButton.setText(R.string.label_retry)
    }

    private fun renderToolbarTitle(title: String) {
        toolbar.title = title.ifBlank { getString(R.string.web_screen_title) }
        toolbar.subtitle = pageUrl
    }

    private fun handleBackPress() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
        }
    }

    private fun shareArticle() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, pageTitle)
            putExtra(Intent.EXTRA_TEXT, "$pageTitle\n$pageUrl")
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.article_share_chooser_title)))
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_SHAREABLE_ARTICLE = "extra_shareable_article"

        fun createIntent(context: Context, title: String, url: String): Intent {
            return Intent(context, WebContainerActivity::class.java)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_URL, url)
        }

        fun createArticleIntent(context: Context, title: String, url: String): Intent {
            return createIntent(context, title, url)
                .putExtra(EXTRA_SHAREABLE_ARTICLE, true)
        }
    }
}
