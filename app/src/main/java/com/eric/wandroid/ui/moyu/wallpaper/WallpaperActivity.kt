package com.eric.wandroid.ui.moyu.wallpaper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.launch

class WallpaperActivity : AppCompatActivity() {
    private val viewModel: WallpaperViewModel by viewModels { WallpaperViewModelFactory() }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var groupTabs: TabLayout
    private lateinit var categoryChips: ChipGroup
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: View
    private lateinit var emptyState: View
    private lateinit var emptyTitle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var retryButton: Button

    private var renderedGroupIndex = -1
    private var lastRefreshError: String? = null
    private val adapter = WallpaperAdapter { url ->
        startActivity(WallpaperPreviewActivity.createIntent(this, url))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper)
        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, refreshLayout)
        setSupportActionBar(toolbar)
        setTitle(R.string.wallpaper_screen_title)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setTitle(R.string.wallpaper_screen_title)

        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.adapter = adapter
        refreshLayout.setOnRefreshListener { viewModel.refresh() }
        retryButton.setOnClickListener { viewModel.retry() }

        WallpaperCategoryCatalog.groups.forEach { group -> groupTabs.addTab(groupTabs.newTab().setText(group.label)) }
        groupTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = viewModel.selectGroup(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        observeUi()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        groupTabs = findViewById(R.id.wallpaperGroupTabs)
        categoryChips = findViewById(R.id.wallpaperCategoryChips)
        refreshLayout = findViewById(R.id.wallpaperRefreshLayout)
        recyclerView = findViewById(R.id.wallpaperRecyclerView)
        progressBar = findViewById(R.id.wallpaperProgress)
        emptyState = findViewById(R.id.wallpaperEmptyState)
        emptyTitle = findViewById(R.id.wallpaperEmptyTitle)
        emptyMessage = findViewById(R.id.wallpaperEmptyMessage)
        retryButton = findViewById(R.id.wallpaperRetryButton)
    }

    private fun observeUi() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: WallpaperUiState) {
        if (renderedGroupIndex != state.selectedGroupIndex) {
            renderedGroupIndex = state.selectedGroupIndex
            groupTabs.getTabAt(state.selectedGroupIndex)?.select()
            renderCategoryChips(state)
        } else {
            updateCheckedCategory(state.selectedCategory)
        }

        adapter.submitUrls(state.wallpapers)
        refreshLayout.isRefreshing = state.isRefreshing
        recyclerView.isVisible = state.wallpapers.isNotEmpty()
        progressBar.isVisible = state.isInitialLoading && state.wallpapers.isEmpty()

        val showError = state.errorMessage != null && state.wallpapers.isEmpty()
        emptyState.isVisible = showError
        if (showError) {
            emptyTitle.setText(R.string.error_title)
            emptyMessage.text = state.errorMessage
        } else if (state.errorMessage != null && state.errorMessage != lastRefreshError) {
            lastRefreshError = state.errorMessage
            Snackbar.make(recyclerView, state.errorMessage, Snackbar.LENGTH_LONG).show()
        } else if (state.errorMessage == null) {
            lastRefreshError = null
        }
    }

    private fun renderCategoryChips(state: WallpaperUiState) {
        categoryChips.removeAllViews()
        WallpaperCategoryCatalog.groups[state.selectedGroupIndex].categories.forEach { category ->
            val chip = Chip(this).apply {
                id = View.generateViewId()
                text = category.label
                isCheckable = true
                isChecked = category == state.selectedCategory
                setOnClickListener { viewModel.selectCategory(category) }
            }
            categoryChips.addView(chip)
        }
    }

    private fun updateCheckedCategory(selectedCategory: WallpaperCategory) {
        for (index in 0 until categoryChips.childCount) {
            val chip = categoryChips.getChildAt(index) as? Chip ?: continue
            chip.isChecked = chip.text == selectedCategory.label
        }
    }

    override fun onDestroy() {
        recyclerView.adapter = null
        super.onDestroy()
    }

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, WallpaperActivity::class.java)
    }
}
