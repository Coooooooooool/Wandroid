package com.eric.wandroid.ui.moyu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MoyuVideoActivity : AppCompatActivity() {
    private val viewModel: MoyuVideoViewModel by viewModels { MoyuVideoViewModelFactory() }

    private lateinit var backButton: ImageButton
    private lateinit var videoControls: View
    private lateinit var viewPager: ViewPager2
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: View
    private lateinit var emptyTitle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var emptyAction: Button

    private val adapter = MoyuVideoPagerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moyu_video)
        bindViews()
        EdgeToEdgeHelper.applyContentOnly(this, videoControls, viewPager, emptyState)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setTitle(R.string.moyu_video_title)
        backButton.setOnClickListener { finish() }

        viewPager.adapter = adapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.updateActivePosition(position)
            }
        })
        emptyAction.setOnClickListener { viewModel.retry() }

        observeUi()
    }

    private fun bindViews() {
        backButton = findViewById(R.id.backButton)
        videoControls = findViewById(R.id.videoControls)
        viewPager = findViewById(R.id.moyuVideoPager)
        progressBar = findViewById(R.id.fullScreenProgress)
        emptyState = findViewById(R.id.emptyState)
        emptyTitle = findViewById(R.id.emptyStateTitle)
        emptyMessage = findViewById(R.id.emptyStateMessage)
        emptyAction = findViewById(R.id.emptyStateAction)
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

    private fun render(state: MoyuVideoUiState) {
        viewPager.visibility = if (state.hasContent) View.VISIBLE else View.GONE
        progressBar.visibility = if (state.isInitialLoading && !state.hasContent) View.VISIBLE else View.GONE

        val showError = !state.hasContent && state.blockingErrorMessage != null
        emptyState.visibility = if (showError) View.VISIBLE else View.GONE
        if (showError) {
            emptyTitle.setText(R.string.error_title)
            emptyMessage.text = state.blockingErrorMessage
            emptyAction.setText(R.string.label_retry)
        }

        adapter.submitItems(state.videoUrls, state.activePosition)
        if (state.hasContent && viewPager.currentItem != state.activePosition) {
            viewPager.setCurrentItem(state.activePosition, false)
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, MoyuVideoActivity::class.java)
        }
    }
}
