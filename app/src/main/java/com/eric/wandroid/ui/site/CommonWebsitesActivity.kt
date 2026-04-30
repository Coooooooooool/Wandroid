package com.eric.wandroid.ui.site

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.eric.wandroid.R
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.HomeRepository
import com.eric.wandroid.domain.model.Website
import com.eric.wandroid.ui.web.WebContainerActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CommonWebsitesActivity : AppCompatActivity() {
    private val repository: HomeRepository by lazy(LazyThreadSafetyMode.NONE) {
        HomeRepository(NetworkModule.apiService)
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var chipGroup: ChipGroup
    private lateinit var fullScreenProgress: ProgressBar
    private lateinit var emptyState: View
    private lateinit var emptyTitle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var emptyAction: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_websites)
        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, swipeRefreshLayout)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        swipeRefreshLayout.setOnRefreshListener { loadWebsites(isRefresh = true) }
        emptyAction.setOnClickListener { loadWebsites(isRefresh = false) }

        loadWebsites(isRefresh = false)
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        chipGroup = findViewById(R.id.websiteChipGroup)
        fullScreenProgress = findViewById(R.id.fullScreenProgress)
        emptyState = findViewById(R.id.emptyState)
        emptyTitle = findViewById(R.id.emptyStateTitle)
        emptyMessage = findViewById(R.id.emptyStateMessage)
        emptyAction = findViewById(R.id.emptyStateAction)
    }

    private fun loadWebsites(isRefresh: Boolean) {
        if (!isRefresh) {
            fullScreenProgress.isVisible = chipGroup.childCount == 0
        }
        emptyState.isVisible = false
        lifecycleScope.launch {
            when (val result = repository.loadCommonWebsites()) {
                is AppResult.Success -> renderContent(result.data)
                is AppResult.Error -> renderError(result.message)
            }
            swipeRefreshLayout.isRefreshing = false
            fullScreenProgress.isVisible = false
        }
    }

    private fun renderContent(websites: List<Website>) {
        chipGroup.removeAllViews()
        websites.forEach { website ->
            val chip = Chip(this)
            chip.text = if (website.category.isBlank()) {
                website.name
            } else {
                "${website.name} - ${website.category}"
            }
            chip.isClickable = true
            chip.isCheckable = false
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.surface_tint)
            )
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            chip.setOnClickListener {
                startActivity(WebContainerActivity.createIntent(this, website.name, website.link))
            }
            chipGroup.addView(chip)
        }
        swipeRefreshLayout.isVisible = true
        emptyState.isVisible = websites.isEmpty()
        if (websites.isEmpty()) {
            emptyTitle.setText(R.string.empty_title)
            emptyMessage.setText(R.string.common_websites_empty_message)
            emptyAction.setText(R.string.label_refresh)
        }
    }

    private fun renderError(message: String) {
        if (chipGroup.childCount > 0) {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
            return
        }
        swipeRefreshLayout.isVisible = false
        emptyState.isVisible = true
        emptyTitle.setText(R.string.error_title)
        emptyMessage.text = message
        emptyAction.setText(R.string.label_retry)
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, CommonWebsitesActivity::class.java)
        }
    }
}
