package com.eric.wandroid.ui.moyu.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.RemoteImageLoader
import com.eric.wandroid.domain.model.NewsArticle

class NewsListAdapter(
    private val onArticleClick: (NewsArticle) -> Unit,
    private val onLoadMoreClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<NewsListItem>()

    fun submitState(state: NewsListStateLike) {
        items.clear()
        items += state.articles.map { NewsListItem.ArticleRow(it) }
        if (state.articles.isNotEmpty()) {
            items += NewsListItem.LoadMore(state.isLoadingMore, state.canLoadMore)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is NewsListItem.ArticleRow -> VIEW_TYPE_ARTICLE
        is NewsListItem.LoadMore -> VIEW_TYPE_LOAD_MORE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_ARTICLE -> ArticleViewHolder(
                inflater.inflate(R.layout.item_news_article, parent, false),
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
            is NewsListItem.ArticleRow -> (holder as ArticleViewHolder).bind(item.article)
            is NewsListItem.LoadMore -> (holder as LoadMoreViewHolder).bind(item)
        }
    }

    private class ArticleViewHolder(
        itemView: View,
        private val onArticleClick: (NewsArticle) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val coverView: ImageView = itemView.findViewById(R.id.newsCover)
        private val titleView: TextView = itemView.findViewById(R.id.newsTitle)
        private val summaryView: TextView = itemView.findViewById(R.id.newsSummary)
        private val metaView: TextView = itemView.findViewById(R.id.newsMeta)

        fun bind(article: NewsArticle) {
            titleView.text = article.title
            val summarySource = buildList {
                if (article.authorName.isNotBlank()) add(article.authorName)
                if (article.date.isNotBlank()) add(article.date)
                if (article.url.isNotBlank()) add(article.url)
            }
            summaryView.text = summarySource.joinToString("\n")
                .ifBlank { itemView.context.getString(R.string.news_open_detail_hint) }
            metaView.text = article.date.ifBlank { itemView.context.getString(R.string.unknown_time) }
            val previewUrl = article.previewImageUrl
            coverView.isVisible = previewUrl.isNotBlank()
            if (previewUrl.isNotBlank()) {
                RemoteImageLoader.loadInto(coverView, previewUrl)
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

        fun bind(item: NewsListItem.LoadMore) {
            progressBar.isVisible = item.isLoading
            button.isEnabled = item.canLoadMore && !item.isLoading
            button.text = when {
                item.isLoading -> itemView.context.getString(R.string.collect_action_loading)
                item.canLoadMore -> itemView.context.getString(R.string.label_load_more)
                else -> itemView.context.getString(R.string.label_no_more)
            }
        }
    }

    private sealed interface NewsListItem {
        data class ArticleRow(val article: NewsArticle) : NewsListItem

        data class LoadMore(
            val isLoading: Boolean,
            val canLoadMore: Boolean
        ) : NewsListItem
    }

    companion object {
        private const val VIEW_TYPE_ARTICLE = 1
        private const val VIEW_TYPE_LOAD_MORE = 2
    }
}
