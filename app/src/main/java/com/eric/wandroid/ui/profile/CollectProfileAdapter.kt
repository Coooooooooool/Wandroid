package com.eric.wandroid.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.ui.web.WebContainerActivity
import com.google.android.material.chip.Chip

class CollectProfileAdapter(
    private val onLoadMoreClick: () -> Unit,
    private val onCollectClick: (Article) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<CollectProfileListItem>()
    private var collectingArticleIds: Set<Int> = emptySet()

    fun submitState(state: CollectProfileUiState) {
        items.clear()
        collectingArticleIds = state.collectingArticleIds

        items += CollectProfileListItem.FavoritesHeader(state.articles.size)
        items += state.articles.map { CollectProfileListItem.ArticleRow(it) }
        if (state.articles.isNotEmpty()) {
            items += CollectProfileListItem.LoadMoreFooter(
                isLoading = state.isLoadingMore,
                canLoadMore = state.canLoadMore
            )
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is CollectProfileListItem.FavoritesHeader -> VIEW_TYPE_HEADER
        is CollectProfileListItem.ArticleRow -> VIEW_TYPE_ARTICLE
        is CollectProfileListItem.LoadMoreFooter -> VIEW_TYPE_LOAD_MORE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> FavoritesHeaderViewHolder(
                inflater.inflate(R.layout.item_system_article_header, parent, false)
            )
            VIEW_TYPE_ARTICLE -> ArticleViewHolder(
                inflater.inflate(R.layout.item_home_article, parent, false),
                onCollectClick
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
            is CollectProfileListItem.FavoritesHeader -> (holder as FavoritesHeaderViewHolder).bind(item.count)
            is CollectProfileListItem.ArticleRow -> (holder as ArticleViewHolder).bind(
                item.article,
                collectingArticleIds.contains(item.article.id)
            )
            is CollectProfileListItem.LoadMoreFooter -> (holder as LoadMoreViewHolder).bind(item)
        }
    }

    private class FavoritesHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.systemArticleTitle)
        private val subtitleView: TextView = itemView.findViewById(R.id.systemArticleSubtitle)

        fun bind(count: Int) {
            titleView.setText(R.string.profile_favorites_title)
            subtitleView.text = itemView.context.getString(R.string.article_section_count, count)
        }
    }

    private class ArticleViewHolder(
        itemView: View,
        private val onCollectClick: (Article) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.articleTitle)
        private val descView: TextView = itemView.findViewById(R.id.articleDescription)
        private val metaView: TextView = itemView.findViewById(R.id.articleMeta)
        private val chapterView: TextView = itemView.findViewById(R.id.articleChapter)
        private val topChip: Chip = itemView.findViewById(R.id.topChip)
        private val collectButton: Button = itemView.findViewById(R.id.collectButton)

        fun bind(article: Article, isCollecting: Boolean) {
            val renderedTitle = HtmlCompat.fromHtml(article.title, HtmlCompat.FROM_HTML_MODE_LEGACY)
            titleView.text = renderedTitle
            descView.text = article.desc.ifBlank { article.link }
            metaView.text = itemView.context.getString(
                R.string.article_meta,
                article.author,
                article.niceDate.ifBlank { itemView.context.getString(R.string.unknown_time) }
            )
            chapterView.text = listOf(article.superChapterName, article.chapterName)
                .filter { it.isNotBlank() }
                .joinToString(" / ")
                .ifBlank { itemView.context.getString(R.string.uncategorized) }
            topChip.isVisible = false
            collectButton.isEnabled = !isCollecting
            collectButton.text = when {
                isCollecting -> itemView.context.getString(R.string.collect_action_loading)
                article.isCollected -> itemView.context.getString(R.string.collect_action_remove)
                else -> itemView.context.getString(R.string.collect_action_add)
            }
            collectButton.setOnClickListener { onCollectClick(article) }
            itemView.setOnClickListener {
                itemView.context.startActivity(
                    WebContainerActivity.createArticleIntent(itemView.context, renderedTitle.toString(), article.link)
                )
            }
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

        fun bind(item: CollectProfileListItem.LoadMoreFooter) {
            progressBar.isVisible = item.isLoading
            button.isEnabled = item.canLoadMore && !item.isLoading
            button.text = when {
                item.isLoading -> itemView.context.getString(R.string.collect_action_loading)
                item.canLoadMore -> itemView.context.getString(R.string.label_load_more)
                else -> itemView.context.getString(R.string.label_no_more)
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_ARTICLE = 2
        private const val VIEW_TYPE_LOAD_MORE = 3
    }
}
