package com.eric.wandroid.ui.project

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
import com.eric.wandroid.domain.model.ProjectCategory
import com.eric.wandroid.ui.web.WebContainerActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ProjectSearchAdapter(
    private val onCategoryClick: (ProjectCategory) -> Unit,
    private val onLoadMoreClick: () -> Unit,
    private val onCollectClick: (Article) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<ProjectSearchListItem>()
    private var collectingArticleIds: Set<Int> = emptySet()

    fun submitState(state: ProjectSearchUiState) {
        items.clear()
        collectingArticleIds = state.collectingArticleIds
        if (state.categories.isNotEmpty()) {
            items += ProjectSearchListItem.CategorySection(state.categories, state.selectedCategoryId)
        }
        items += ProjectSearchListItem.ArticleHeader(state.selectedCategoryName, state.articles.size)
        items += state.articles.map { ProjectSearchListItem.ArticleRow(it) }
        if (state.articles.isNotEmpty()) {
            items += ProjectSearchListItem.LoadMoreFooter(state.isLoadingMore, state.canLoadMore)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ProjectSearchListItem.CategorySection -> VIEW_TYPE_CATEGORY_SECTION
        is ProjectSearchListItem.ArticleHeader -> VIEW_TYPE_ARTICLE_HEADER
        is ProjectSearchListItem.ArticleRow -> VIEW_TYPE_ARTICLE
        is ProjectSearchListItem.LoadMoreFooter -> VIEW_TYPE_LOAD_MORE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_CATEGORY_SECTION -> CategorySectionViewHolder(
                inflater.inflate(R.layout.item_project_category_section, parent, false),
                onCategoryClick
            )
            VIEW_TYPE_ARTICLE_HEADER -> ArticleHeaderViewHolder(
                inflater.inflate(R.layout.item_project_article_header, parent, false)
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
            is ProjectSearchListItem.CategorySection -> (holder as CategorySectionViewHolder).bind(item)
            is ProjectSearchListItem.ArticleHeader -> (holder as ArticleHeaderViewHolder).bind(item)
            is ProjectSearchListItem.ArticleRow -> (holder as ArticleViewHolder).bind(
                item.article,
                collectingArticleIds.contains(item.article.id)
            )
            is ProjectSearchListItem.LoadMoreFooter -> (holder as LoadMoreViewHolder).bind(item)
        }
    }

    private class CategorySectionViewHolder(
        itemView: View,
        private val onCategoryClick: (ProjectCategory) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val chipGroup: ChipGroup = itemView.findViewById(R.id.projectCategoryChipGroup)

        fun bind(item: ProjectSearchListItem.CategorySection) {
            chipGroup.removeAllViews()
            item.categories.forEach { category ->
                val chip = Chip(itemView.context)
                chip.text = category.name
                chip.isCheckable = true
                chip.isChecked = category.id == item.selectedCategoryId
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
        private val titleView: TextView = itemView.findViewById(R.id.projectArticleTitle)
        private val countView: TextView = itemView.findViewById(R.id.projectArticleCount)

        fun bind(item: ProjectSearchListItem.ArticleHeader) {
            titleView.text = item.title.ifBlank { itemView.context.getString(R.string.project_articles_title) }
            countView.text = itemView.context.getString(R.string.system_article_count, item.count)
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
                    WebContainerActivity.createIntent(itemView.context, renderedTitle.toString(), article.link)
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

        fun bind(item: ProjectSearchListItem.LoadMoreFooter) {
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
        private const val VIEW_TYPE_CATEGORY_SECTION = 1
        private const val VIEW_TYPE_ARTICLE_HEADER = 2
        private const val VIEW_TYPE_ARTICLE = 3
        private const val VIEW_TYPE_LOAD_MORE = 4
    }
}
