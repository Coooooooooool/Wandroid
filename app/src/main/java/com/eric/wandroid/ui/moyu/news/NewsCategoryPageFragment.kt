package com.eric.wandroid.ui.moyu.news

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.ScrollToTopHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class NewsCategoryPageFragment : Fragment() {
    private val categoryType: String by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(ARG_CATEGORY_TYPE).orEmpty()
    }

    private lateinit var viewModel: NewsCategoryPageViewModel

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: View
    private lateinit var emptyTitle: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var emptyAction: Button
    private lateinit var scrollToTopButton: FloatingActionButton

    private val adapter = NewsListAdapter(
        onArticleClick = { startActivity(NewsDetailActivity.createIntent(requireContext(), it)) },
        onLoadMoreClick = { viewModel.loadMore() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_news_category_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            NewsCategoryPageViewModelFactory(categoryType = categoryType)
        )[NewsCategoryPageViewModel::class.java]
        bindViews(view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        ScrollToTopHelper.attach(recyclerView, scrollToTopButton) { recyclerView.visibility != View.VISIBLE }
        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
        emptyAction.setOnClickListener { viewModel.retry() }
        observeUi()
    }

    private fun bindViews(view: View) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        recyclerView = view.findViewById(R.id.newsRecyclerView)
        progressBar = view.findViewById(R.id.fullScreenProgress)
        emptyState = view.findViewById(R.id.emptyState)
        emptyTitle = view.findViewById(R.id.emptyStateTitle)
        emptyMessage = view.findViewById(R.id.emptyStateMessage)
        emptyAction = view.findViewById(R.id.emptyStateAction)
        scrollToTopButton = view.findViewById(R.id.scrollToTopButton)
    }

    private fun observeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.messages.collect { message ->
                        view?.let { root ->
                            Snackbar.make(root, message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun render(state: NewsCategoryPageUiState) {
        swipeRefreshLayout.isRefreshing = state.isRefreshing
        recyclerView.visibility = if (state.hasContent) View.VISIBLE else View.GONE
        progressBar.visibility = if (state.isInitialLoading && !state.hasContent) View.VISIBLE else View.GONE
        adapter.submitState(
            NewsCategoryListState(
                articles = state.articles,
                isLoadingMore = state.isLoadingMore,
                canLoadMore = state.canLoadMore
            )
        )
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
                emptyMessage.setText(R.string.news_empty_message)
                emptyAction.setText(R.string.label_refresh)
            }
        }
    }

    companion object {
        private const val ARG_CATEGORY_TYPE = "arg_category_type"

        fun newInstance(categoryType: String): NewsCategoryPageFragment {
            return NewsCategoryPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_TYPE, categoryType)
                }
            }
        }
    }
}

private data class NewsCategoryListState(
    override val articles: List<com.eric.wandroid.domain.model.NewsArticle>,
    override val isLoadingMore: Boolean,
    override val canLoadMore: Boolean
) : NewsListStateLike
