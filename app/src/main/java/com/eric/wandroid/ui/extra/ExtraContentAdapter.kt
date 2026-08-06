package com.eric.wandroid.ui.extra

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
import com.eric.wandroid.data.repository.ExtraContentMode
import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.ProjectCategory
import com.eric.wandroid.ui.wenda.WendaDetailActivity
import com.eric.wandroid.ui.web.WebContainerActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ExtraContentAdapter(
    private val onWechatCategoryClick: (ProjectCategory) -> Unit,
    private val onLoadMoreClick: () -> Unit,
    private val onCollectClick: (Article) -> Unit,
    private val onShareUserClick: (Article) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<ExtraContentListItem>()
    private var collectingArticleIds: Set<Int> = emptySet()
    private var currentMode = ExtraContentMode.Wenda

    fun submitState(state: ExtraContentUiState) {
        items.clear()
        collectingArticleIds = state.collectingArticleIds
        currentMode = state.mode

        if (state.mode == ExtraContentMode.Wechat && state.wechatCategories.isNotEmpty()) {
            items += ExtraContentListItem.WechatCategorySection(
                categories = state.wechatCategories,
                selectedCategoryId = state.selectedWechatCategoryId
            )
        }

        val headerTitle = when (state.mode) {
            ExtraContentMode.Wenda -> "\u95EE\u7B54"
            ExtraContentMode.Square -> "\u5E7F\u573A"
            ExtraContentMode.Wechat -> {
                state.selectedWechatCategoryName.ifBlank { "\u516C\u4F17\u53F7" }
            }
            ExtraContentMode.LatestProject -> "\u6700\u65B0\u9879\u76EE"
            ExtraContentMode.ShareUser -> state.shareUserName.ifBlank { "\u5206\u4EAB\u5217\u8868" }
            ExtraContentMode.PrivateShare -> "\u6211\u7684\u5206\u4EAB"
        }
        items += ExtraContentListItem.ArticleHeader(headerTitle, state.articles.size)
        items += state.articles.map { ExtraContentListItem.ArticleRow(it) }
        if (state.articles.isNotEmpty()) {
            items += ExtraContentListItem.LoadMoreFooter(state.isLoadingMore, state.canLoadMore)
        }

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ExtraContentListItem.WechatCategorySection -> VIEW_TYPE_WECHAT_CATEGORY
        is ExtraContentListItem.ArticleHeader -> VIEW_TYPE_ARTICLE_HEADER
        is ExtraContentListItem.ArticleRow -> {
            if (currentMode == ExtraContentMode.Wenda) {
                VIEW_TYPE_WENDA_ARTICLE
            } else {
                VIEW_TYPE_ARTICLE
            }
        }
        is ExtraContentListItem.LoadMoreFooter -> VIEW_TYPE_LOAD_MORE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_WECHAT_CATEGORY -> WechatCategorySectionViewHolder(
                inflater.inflate(R.layout.item_extra_chip_section, parent, false),
                onWechatCategoryClick
            )

            VIEW_TYPE_ARTICLE_HEADER -> ArticleHeaderViewHolder(
                inflater.inflate(R.layout.item_system_article_header, parent, false)
            )

            VIEW_TYPE_ARTICLE -> ArticleViewHolder(
                inflater.inflate(R.layout.item_home_article, parent, false),
                onCollectClick
            )

            VIEW_TYPE_WENDA_ARTICLE -> ArticleViewHolder(
                inflater.inflate(R.layout.item_wenda_list_article, parent, false),
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
            is ExtraContentListItem.WechatCategorySection -> (holder as WechatCategorySectionViewHolder).bind(item)
            is ExtraContentListItem.ArticleHeader -> (holder as ArticleHeaderViewHolder).bind(item)
            is ExtraContentListItem.ArticleRow -> (holder as ArticleViewHolder).bind(
                item.article,
                currentMode == ExtraContentMode.Wenda,
                collectingArticleIds.contains(item.article.id),
                currentMode,
                onShareUserClick
            )
            is ExtraContentListItem.LoadMoreFooter -> (holder as LoadMoreViewHolder).bind(item)
        }
    }

    private class WechatCategorySectionViewHolder(
        itemView: View,
        private val onCategoryClick: (ProjectCategory) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.sectionTitle)
        private val chipGroup: ChipGroup = itemView.findViewById(R.id.chipGroup)

        fun bind(item: ExtraContentListItem.WechatCategorySection) {
            titleView.text = "\u516C\u4F17\u53F7\u5206\u7C7B"
            chipGroup.removeAllViews()
            item.categories.forEach { category ->
                val chip = Chip(itemView.context).apply {
                    text = category.name
                    isCheckable = true
                    isChecked = category.id == item.selectedCategoryId
                    checkedIcon = null
                    chipBackgroundColor = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            itemView.context,
                            if (isChecked) R.color.brand_secondary else R.color.surface_tint
                        )
                    )
                    setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            if (isChecked) R.color.white else R.color.text_primary
                        )
                    )
                    setOnClickListener { onCategoryClick(category) }
                }
                chipGroup.addView(chip)
            }
        }
    }

    private class ArticleHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.systemArticleTitle)
        private val subtitleView: TextView = itemView.findViewById(R.id.systemArticleSubtitle)

        fun bind(item: ExtraContentListItem.ArticleHeader) {
            titleView.text = item.title
            subtitleView.text = itemView.context.getString(R.string.system_article_count, item.count)
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

        fun bind(
            article: Article,
            hideDescription: Boolean,
            isCollecting: Boolean,
            mode: ExtraContentMode,
            onShareUserClick: (Article) -> Unit
        ) {
            val renderedTitle = HtmlCompat.fromHtml(article.title, HtmlCompat.FROM_HTML_MODE_LEGACY)
            titleView.text = renderedTitle
            descView.isVisible = !hideDescription
            if (!hideDescription) {
                descView.text = article.desc.ifBlank { article.link }
            } else {
                descView.text = ""
            }
            val authorName = article.author.ifBlank { article.shareUser }
            val articleDate = article.niceDate.ifBlank { itemView.context.getString(R.string.unknown_time) }
            metaView.text = if (mode == ExtraContentMode.Square && article.userId > 0 && authorName.isNotBlank()) {
                itemView.context.getString(R.string.square_article_meta_with_hint, authorName, articleDate)
            } else {
                itemView.context.getString(R.string.article_meta, authorName, articleDate)
            }
            chapterView.text = listOf(article.superChapterName, article.chapterName)
                .filter { it.isNotBlank() }
                .joinToString(" / ")
                .ifBlank { itemView.context.getString(R.string.uncategorized) }
            topChip.isVisible = article.isTopPinned
            metaView.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (mode == ExtraContentMode.Square && article.userId > 0) {
                        R.color.brand_secondary
                    } else {
                        R.color.text_secondary
                    }
                )
            )
            collectButton.isEnabled = !isCollecting
            collectButton.text = when {
                isCollecting -> itemView.context.getString(R.string.collect_action_loading)
                mode == ExtraContentMode.PrivateShare -> itemView.context.getString(R.string.square_share_delete_action)
                article.isCollected -> itemView.context.getString(R.string.collect_action_remove)
                else -> itemView.context.getString(R.string.collect_action_add)
            }
            collectButton.setOnClickListener { onCollectClick(article) }
            metaView.setOnClickListener {
                if (mode == ExtraContentMode.Square && article.userId > 0) {
                    onShareUserClick(article)
                }
            }
            itemView.setOnClickListener {
                val intent = if (mode == ExtraContentMode.Wenda && article.id > 0) {
                    WendaDetailActivity.createIntent(itemView.context, article)
                } else {
                    WebContainerActivity.createArticleIntent(itemView.context, renderedTitle.toString(), article.link)
                }
                itemView.context.startActivity(intent)
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

        fun bind(item: ExtraContentListItem.LoadMoreFooter) {
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
        private const val VIEW_TYPE_WECHAT_CATEGORY = 1
        private const val VIEW_TYPE_ARTICLE_HEADER = 2
        private const val VIEW_TYPE_ARTICLE = 3
        private const val VIEW_TYPE_LOAD_MORE = 4
        private const val VIEW_TYPE_WENDA_ARTICLE = 5
    }
}
