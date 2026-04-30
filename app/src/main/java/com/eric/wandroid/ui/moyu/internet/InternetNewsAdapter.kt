package com.eric.wandroid.ui.moyu.internet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.RemoteImageLoader
import com.eric.wandroid.domain.model.InternetNewsArticle

class InternetNewsAdapter(
    private val onSearchSubmit: (String) -> Unit,
    private val onArticleClick: (InternetNewsArticle) -> Unit,
    private val onLoadMoreClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<InternetNewsListItem>()

    fun submitState(state: InternetNewsUiState) {
        items.clear()
        items += InternetNewsListItem.SearchBox(state.keyword)
        items += state.articles.map { InternetNewsListItem.ArticleRow(it) }
        if (state.articles.isNotEmpty()) {
            items += InternetNewsListItem.LoadMore(state.isLoadingMore, state.canLoadMore)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is InternetNewsListItem.SearchBox -> VIEW_TYPE_SEARCH
        is InternetNewsListItem.ArticleRow -> VIEW_TYPE_ARTICLE
        is InternetNewsListItem.LoadMore -> VIEW_TYPE_LOAD_MORE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SEARCH -> SearchViewHolder(
                inflater.inflate(R.layout.item_internet_news_search, parent, false),
                onSearchSubmit
            )
            VIEW_TYPE_ARTICLE -> ArticleViewHolder(
                inflater.inflate(R.layout.item_internet_news_article, parent, false),
                onArticleClick
            )
            VIEW_TYPE_LOAD_MORE -> LoadMoreViewHolder(
                inflater.inflate(R.layout.item_home_load_more, parent, false),
                onLoadMoreClick
            )
            else -> error("Unsupported view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is InternetNewsListItem.SearchBox -> (holder as SearchViewHolder).bind(item)
            is InternetNewsListItem.ArticleRow -> (holder as ArticleViewHolder).bind(item.article)
            is InternetNewsListItem.LoadMore -> (holder as LoadMoreViewHolder).bind(item)
        }
    }

    private class SearchViewHolder(
        itemView: View,
        private val onSearchSubmit: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val inputView: EditText = itemView.findViewById(R.id.searchInput)
        private val button: Button = itemView.findViewById(R.id.searchButton)

        init {
            button.setOnClickListener {
                onSearchSubmit(inputView.text?.toString().orEmpty())
            }
        }

        fun bind(item: InternetNewsListItem.SearchBox) {
            if (inputView.text?.toString() != item.keyword) {
                inputView.setText(item.keyword)
                inputView.setSelection(item.keyword.length)
            }
        }
    }

    private class ArticleViewHolder(
        itemView: View,
        private val onArticleClick: (InternetNewsArticle) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val coverView: ImageView = itemView.findViewById(R.id.newsCover)
        private val titleView: TextView = itemView.findViewById(R.id.newsTitle)
        private val summaryView: TextView = itemView.findViewById(R.id.newsSummary)
        private val metaView: TextView = itemView.findViewById(R.id.newsMeta)

        fun bind(article: InternetNewsArticle) {
            titleView.text = article.title
            summaryView.text = article.summary.ifBlank { article.url }
            metaView.text = listOf(article.source, article.time)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
                .ifBlank { itemView.context.getString(R.string.unknown_time) }
            coverView.isVisible = article.imageUrl.isNotBlank()
            if (article.imageUrl.isNotBlank()) {
                RemoteImageLoader.loadInto(coverView, article.imageUrl)
            }
            itemView.setOnClickListener { onArticleClick(article) }
        }
    }

    private class LoadMoreViewHolder(
        itemView: View,
        onLoadMoreClick: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val button: Button = itemView.findViewById(R.id.loadMoreButton)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.loadMoreProgress)

        init {
            button.setOnClickListener { onLoadMoreClick() }
        }

        fun bind(item: InternetNewsListItem.LoadMore) {
            progressBar.isVisible = item.isLoading
            button.isEnabled = item.canLoadMore && !item.isLoading
            button.text = when {
                item.isLoading -> itemView.context.getString(R.string.collect_action_loading)
                item.canLoadMore -> itemView.context.getString(R.string.label_load_more)
                else -> itemView.context.getString(R.string.label_no_more)
            }
        }
    }

    private sealed interface InternetNewsListItem {
        data class SearchBox(val keyword: String) : InternetNewsListItem
        data class ArticleRow(val article: InternetNewsArticle) : InternetNewsListItem
        data class LoadMore(val isLoading: Boolean, val canLoadMore: Boolean) : InternetNewsListItem
    }

    companion object {
        private const val VIEW_TYPE_SEARCH = 1
        private const val VIEW_TYPE_ARTICLE = 2
        private const val VIEW_TYPE_LOAD_MORE = 3
    }
}
