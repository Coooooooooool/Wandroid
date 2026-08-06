package com.eric.wandroid.ui.system

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.SystemChildCategory
import com.eric.wandroid.ui.web.WebContainerActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class SystemDetailAdapter(
    private val onCategoryClick: (SystemChildCategory) -> Unit,
    private val onMoreCategoriesClick: () -> Unit,
    private val onLoadMoreClick: () -> Unit,
    private val onCollectClick: (Article) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<SystemDetailListItem>()
    private var collectingArticleIds: Set<Int> = emptySet()

    fun submitState(state: SystemDetailUiState) {
        items.clear()
        collectingArticleIds = state.collectingArticleIds

        if (state.children.isNotEmpty()) {
            items += SystemDetailListItem.CategorySelector(
                categories = state.children,
                selectedCategoryId = state.selectedCategoryId
            )
        }
        if (state.selectedCategoryName.isNotBlank() || state.articles.isNotEmpty()) {
            items += SystemDetailListItem.ArticleHeader(
                categoryName = state.selectedCategoryName,
                count = state.articles.size
            )
        }
        if (state.articles.isNotEmpty()) {
            items += state.articles.map { SystemDetailListItem.ArticleRow(it) }
            items += SystemDetailListItem.LoadMoreFooter(
                isLoading = state.isLoadingMore,
                canLoadMore = state.canLoadMore
            )
        }

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is SystemDetailListItem.CategorySelector -> VIEW_TYPE_SELECTOR
        is SystemDetailListItem.ArticleHeader -> VIEW_TYPE_HEADER
        is SystemDetailListItem.ArticleRow -> VIEW_TYPE_ARTICLE
        is SystemDetailListItem.LoadMoreFooter -> VIEW_TYPE_LOAD_MORE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SELECTOR -> CategorySelectorViewHolder(
                inflater.inflate(R.layout.item_system_category_selector, parent, false),
                onCategoryClick,
                onMoreCategoriesClick
            )
            VIEW_TYPE_HEADER -> ArticleHeaderViewHolder(
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
            is SystemDetailListItem.CategorySelector ->
                (holder as CategorySelectorViewHolder).bind(item.categories, item.selectedCategoryId)
            is SystemDetailListItem.ArticleHeader ->
                (holder as ArticleHeaderViewHolder).bind(item.categoryName, item.count)
            is SystemDetailListItem.ArticleRow ->
                (holder as ArticleViewHolder).bind(item.article, collectingArticleIds.contains(item.article.id))
            is SystemDetailListItem.LoadMoreFooter ->
                (holder as LoadMoreViewHolder).bind(item)
        }
    }

    private class CategorySelectorViewHolder(
        itemView: View,
        private val onCategoryClick: (SystemChildCategory) -> Unit,
        private val onMoreCategoriesClick: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.sectionTitle)
        private val actionView: TextView = itemView.findViewById(R.id.sectionAction)
        private val chipGroup: ChipGroup = itemView.findViewById(R.id.chipGroup)

        fun bind(categories: List<SystemChildCategory>, selectedCategoryId: Int?) {
            titleView.setText(R.string.system_detail_child_title)
            actionView.isVisible = categories.size > 6
            actionView.setText(R.string.system_detail_more_categories)
            actionView.setOnClickListener { onMoreCategoriesClick() }

            chipGroup.removeAllViews()
            categories.take(6).forEach { category ->
                val chip = Chip(itemView.context)
                chip.text = category.name
                chip.isCheckable = true
                chip.isChecked = category.id == selectedCategoryId
                chip.checkedIcon = null
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        itemView.context,
                        if (chip.isChecked) R.color.brand_secondary else R.color.surface_tint
                    )
                )
                chip.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        if (chip.isChecked) R.color.white else R.color.text_primary
                    )
                )
                chip.setOnClickListener { onCategoryClick(category) }
                chipGroup.addView(chip)
            }
        }
    }

    private class ArticleHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.systemArticleTitle)
        private val subtitleView: TextView = itemView.findViewById(R.id.systemArticleSubtitle)

        fun bind(categoryName: String, count: Int) {
            titleView.text = categoryName.ifBlank { itemView.context.getString(R.string.system_articles_title) }
            subtitleView.text = itemView.context.getString(R.string.system_article_count, count)
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

        fun bind(item: SystemDetailListItem.LoadMoreFooter) {
            progressBar.isVisible = item.isLoading
            button.isEnabled = item.canLoadMore && !item.isLoading
            button.text = when {
                item.isLoading -> itemView.context.getString(R.string.system_loading_articles)
                item.canLoadMore -> itemView.context.getString(R.string.label_load_more)
                else -> itemView.context.getString(R.string.label_no_more)
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_SELECTOR = 1
        private const val VIEW_TYPE_HEADER = 2
        private const val VIEW_TYPE_ARTICLE = 3
        private const val VIEW_TYPE_LOAD_MORE = 4
    }
}
