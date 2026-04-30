package com.eric.wandroid.ui.shell

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
import com.eric.wandroid.common.auth.navigateToLogin
import com.eric.wandroid.common.ui.ScrollToTopHelper
import com.eric.wandroid.ui.home.HomeAdapter
import com.eric.wandroid.ui.home.HomeUiState
import com.eric.wandroid.ui.home.HomeViewModel
import com.eric.wandroid.ui.home.HomeViewModelFactory
import com.eric.wandroid.ui.search.SearchActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class HomeFeedFragment : Fragment() {
    private val viewModel: HomeViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, HomeViewModelFactory())[HomeViewModel::class.java]
    }

    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var recyclerView: RecyclerView? = null
    private var fullScreenProgress: ProgressBar? = null
    private var emptyState: View? = null
    private var emptyTitle: TextView? = null
    private var emptyMessage: TextView? = null
    private var actionButton: Button? = null
    private var scrollToTopButton: FloatingActionButton? = null

    private val homeAdapter = HomeAdapter(
        onSearchClick = {
            startActivity(SearchActivity.createIntent(requireContext()))
        },
        onLoadMoreClick = { viewModel.loadMore() },
        onCollectClick = { viewModel.toggleCollect(it) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        recyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = homeAdapter
            scrollToTopButton?.let { button ->
                ScrollToTopHelper.attach(this, button) { visibility != View.VISIBLE }
            }
        }

        swipeRefreshLayout?.setOnRefreshListener { viewModel.refresh() }
        actionButton?.setOnClickListener { viewModel.retry() }

        observeUi()
    }

    override fun onResume() {
        super.onResume()
        homeAdapter.setBannerAutoScrollEnabled(true)
    }

    override fun onPause() {
        homeAdapter.setBannerAutoScrollEnabled(false)
        super.onPause()
    }

    private fun bindViews(view: View) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        recyclerView = view.findViewById(R.id.homeRecyclerView)
        fullScreenProgress = view.findViewById(R.id.fullScreenProgress)
        emptyState = view.findViewById(R.id.emptyState)
        emptyTitle = view.findViewById(R.id.emptyStateTitle)
        emptyMessage = view.findViewById(R.id.emptyStateMessage)
        actionButton = view.findViewById(R.id.emptyStateAction)
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
                launch {
                    viewModel.authRequired.collect { message ->
                        activity?.navigateToLogin(message)
                    }
                }
            }
        }
    }

    private fun render(state: HomeUiState) {
        swipeRefreshLayout?.isRefreshing = state.isRefreshing
        homeAdapter.submitState(state)

        val showBlockingState = !state.hasContent && state.blockingErrorMessage != null
        val showEmptyState = !state.hasContent && !state.isInitialLoading && state.blockingErrorMessage == null

        recyclerView?.visibility = if (state.hasContent) View.VISIBLE else View.GONE
        fullScreenProgress?.visibility = if (state.isInitialLoading && !state.hasContent) View.VISIBLE else View.GONE
        emptyState?.visibility = if (showBlockingState || showEmptyState) View.VISIBLE else View.GONE
        recyclerView?.let { list ->
            scrollToTopButton?.let { button ->
                ScrollToTopHelper.update(list, button, !state.hasContent)
            }
        }

        if (showBlockingState) {
            emptyTitle?.setText(R.string.error_title)
            emptyMessage?.text = state.blockingErrorMessage
            actionButton?.setText(R.string.label_retry)
        } else {
            emptyTitle?.setText(R.string.empty_title)
            emptyMessage?.setText(R.string.empty_message)
            actionButton?.setText(R.string.label_refresh)
        }
    }

    override fun onDestroyView() {
        recyclerView?.adapter = null
        swipeRefreshLayout = null
        recyclerView = null
        fullScreenProgress = null
        emptyState = null
        emptyTitle = null
        emptyMessage = null
        actionButton = null
        scrollToTopButton = null
        super.onDestroyView()
    }
}
