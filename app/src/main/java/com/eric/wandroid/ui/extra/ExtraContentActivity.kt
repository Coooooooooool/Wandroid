package com.eric.wandroid.ui.extra

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
import com.eric.wandroid.data.repository.ExtraContentMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ExtraContentActivity : AppCompatActivity() {
    private val initialMode: ExtraContentMode by lazy(LazyThreadSafetyMode.NONE) {
        resolveInitialMode() ?: ExtraContentMode.Wenda
    }
    private val initialShareUserId: Int? by lazy(LazyThreadSafetyMode.NONE) {
        val value = intent.getIntExtra(EXTRA_SHARE_USER_ID, Int.MIN_VALUE)
        if (value == Int.MIN_VALUE) null else value
    }
    private val initialShareUserName: String by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra(EXTRA_SHARE_USER_NAME).orEmpty()
    }

    private val viewModel: ExtraContentViewModel by viewModels {
        ExtraContentViewModelFactory(
            initialMode = initialMode,
            initialShareUserId = initialShareUserId,
            initialShareUserName = initialShareUserName
        )
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

    private val adapter = ExtraContentAdapter(
        onWechatCategoryClick = { viewModel.selectWechatCategory(it) },
        onLoadMoreClick = { viewModel.loadMore() },
        onCollectClick = {
            if (viewModel.uiState.value.mode == ExtraContentMode.PrivateShare) {
                confirmDeleteSharedArticle(it)
            } else {
                viewModel.toggleCollect(it)
            }
        },
        onShareUserClick = { article ->
            if (article.userId > 0) {
                startActivity(createShareUserIntent(this, article.userId, article.author))
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extra_content)
        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, recyclerView)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_add_share) {
                showShareEditor()
                true
            } else {
                false
            }
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ExtraContentActivity)
            adapter = this@ExtraContentActivity.adapter
        }
        ScrollToTopHelper.attach(recyclerView, scrollToTopButton) { !recyclerView.isVisible }

        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
        emptyAction.setOnClickListener { viewModel.retry() }

        observeUi()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.extraRecyclerView)
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

    private fun resolveInitialMode(): ExtraContentMode? {
        val rawMode = intent.getStringExtra(EXTRA_INITIAL_MODE) ?: return null
        return ExtraContentMode.entries.firstOrNull { it.name == rawMode }
    }

    private fun render(state: ExtraContentUiState) {
        toolbar.title = when (state.mode) {
            ExtraContentMode.Wenda -> getString(R.string.discover_wenda_title)
            ExtraContentMode.Square -> getString(R.string.discover_square_title)
            ExtraContentMode.Wechat -> getString(R.string.discover_wechat_title)
            ExtraContentMode.LatestProject -> getString(R.string.projects_latest_title)
            ExtraContentMode.ShareUser -> {
                val userName = state.shareUserName.ifBlank { initialShareUserName }
                if (userName.isBlank()) {
                    getString(R.string.square_share_user_title)
                } else {
                    getString(R.string.square_share_user_title_with_name, userName)
                }
            }
            ExtraContentMode.PrivateShare -> getString(R.string.square_my_share_title)
        }
        toolbar.subtitle = when (state.mode) {
            ExtraContentMode.ShareUser -> null
            ExtraContentMode.PrivateShare -> getString(R.string.square_my_share_subtitle)
            else -> null
        }
        toolbar.menu.clear()
        if (state.mode == ExtraContentMode.PrivateShare) {
            toolbar.inflateMenu(R.menu.menu_private_share)
        }

        swipeRefreshLayout.isRefreshing = state.isRefreshing
        adapter.submitState(state)

        val showBlockingState = !state.hasContent && state.blockingErrorMessage != null
        val showEmptyState = !state.hasContent && !state.isInitialLoading && state.blockingErrorMessage == null

        recyclerView.isVisible = state.hasContent
        fullScreenProgress.isVisible =
            (state.isInitialLoading && !state.hasContent) ||
                (state.isCategoryLoading && state.articles.isEmpty() && state.mode == ExtraContentMode.Wechat)
        emptyState.isVisible = showBlockingState || showEmptyState
        ScrollToTopHelper.update(recyclerView, scrollToTopButton, !state.hasContent)

        if (showBlockingState) {
            emptyTitle.setText(R.string.error_title)
            emptyMessage.text = state.blockingErrorMessage
            emptyAction.setText(R.string.label_retry)
        } else {
            emptyTitle.setText(R.string.empty_title)
            emptyMessage.setText(R.string.extra_fixed_empty_message)
            emptyAction.setText(R.string.label_refresh)
        }
    }

    companion object {
        private const val EXTRA_INITIAL_MODE = "extra_initial_mode"
        private const val EXTRA_SHARE_USER_ID = "extra_share_user_id"
        private const val EXTRA_SHARE_USER_NAME = "extra_share_user_name"

        fun createIntent(context: Context, initialMode: ExtraContentMode): Intent {
            return Intent(context, ExtraContentActivity::class.java)
                .putExtra(EXTRA_INITIAL_MODE, initialMode.name)
        }

        fun createShareUserIntent(context: Context, userId: Int, userName: String): Intent {
            return Intent(context, ExtraContentActivity::class.java)
                .putExtra(EXTRA_INITIAL_MODE, ExtraContentMode.ShareUser.name)
                .putExtra(EXTRA_SHARE_USER_ID, userId)
                .putExtra(EXTRA_SHARE_USER_NAME, userName)
        }
    }

    private fun showShareEditor() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_share_article, null, false)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.shareCancelButton)
        val saveButton = dialogView.findViewById<MaterialButton>(R.id.shareSaveButton)
        val titleLayout = dialogView.findViewById<TextInputLayout>(R.id.shareTitleLayout)
        val linkLayout = dialogView.findViewById<TextInputLayout>(R.id.shareLinkLayout)
        val titleInput = dialogView.findViewById<TextInputEditText>(R.id.shareTitleInput)
        val linkInput = dialogView.findViewById<TextInputEditText>(R.id.shareLinkInput)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)
        cancelButton.setOnClickListener { dialog.dismiss() }
        saveButton.setOnClickListener {
            titleLayout.error = null
            linkLayout.error = null
            val title = titleInput.text?.toString().orEmpty().trim()
            val link = linkInput.text?.toString().orEmpty().trim()
            var hasError = false
            if (title.isBlank()) {
                titleLayout.error = getString(R.string.square_share_title_required)
                hasError = true
            }
            if (link.isBlank()) {
                linkLayout.error = getString(R.string.square_share_link_required)
                hasError = true
            }
            if (hasError) return@setOnClickListener
            viewModel.submitShare(title, link)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun confirmDeleteSharedArticle(article: com.eric.wandroid.domain.model.Article) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.square_share_delete_title)
            .setMessage(getString(R.string.square_share_delete_message, article.title))
            .setNegativeButton(R.string.todo_cancel_action, null)
            .setPositiveButton(R.string.todo_delete_action) { _, _ ->
                viewModel.deleteSharedArticle(article)
            }
            .show()
    }
}
