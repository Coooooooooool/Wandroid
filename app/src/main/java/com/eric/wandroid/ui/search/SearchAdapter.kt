package com.eric.wandroid.ui.search

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.HotKey
import com.eric.wandroid.domain.model.SearchHistoryItem
import com.eric.wandroid.ui.web.WebContainerActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class SearchAdapter(
    private val onSearchSubmit: (String) -> Unit,
    private val onHotKeyClick: (String) -> Unit,
    private val onSearchHistoryClick: (String) -> Unit,
    private val onSearchHistoryLongClick: (String) -> Unit,
    private val onClearSearchHistory: () -> Unit,
    private val onClearSearch: () -> Unit,
    private val onLoadMoreClick: () -> Unit,
    private val onCollectClick: (Article) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<SearchListItem>()
    private var collectingArticleIds: Set<Int> = emptySet()

    fun submitState(state: SearchUiState) {
        items.clear()
        collectingArticleIds = state.collectingArticleIds
        items += SearchListItem.SearchBox(state.keyword, state.isResultMode)
        if (state.isResultMode) {
            items += SearchListItem.ResultHeader(state.keyword, state.articles.size)
            items += state.articles.map { SearchListItem.ArticleRow(it) }
            if (state.articles.isNotEmpty()) {
                items += SearchListItem.LoadMoreFooter(state.isLoadingMore, state.canLoadMore)
            }
        } else {
            if (state.searchHistory.isNotEmpty()) {
                items += SearchListItem.SearchHistorySection(state.searchHistory)
            }
            if (state.hotKeys.isNotEmpty()) {
                items += SearchListItem.HotKeySection(state.hotKeys)
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is SearchListItem.SearchBox -> VIEW_TYPE_SEARCH_BOX
        is SearchListItem.SearchHistorySection -> VIEW_TYPE_SEARCH_HISTORY
        is SearchListItem.HotKeySection -> VIEW_TYPE_HOT_KEYS
        is SearchListItem.ResultHeader -> VIEW_TYPE_RESULT_HEADER
        is SearchListItem.ArticleRow -> VIEW_TYPE_ARTICLE
        is SearchListItem.LoadMoreFooter -> VIEW_TYPE_LOAD_MORE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SEARCH_BOX -> SearchBoxViewHolder(
                inflater.inflate(R.layout.item_search_box, parent, false),
                onSearchSubmit,
                onClearSearch
            )
            VIEW_TYPE_HOT_KEYS -> HotKeySectionViewHolder(
                inflater.inflate(R.layout.item_home_chip_section, parent, false),
                titleRes = R.string.search_hot_keys_title,
                actionTextRes = null,
                onActionClick = null,
                onChipClick = onHotKeyClick
            )
            VIEW_TYPE_SEARCH_HISTORY -> HotKeySectionViewHolder(
                inflater.inflate(R.layout.item_home_chip_section, parent, false),
                titleRes = R.string.search_history_title,
                actionTextRes = R.string.search_history_clear_all,
                onActionClick = onClearSearchHistory,
                onChipClick = onSearchHistoryClick,
                onChipLongClick = onSearchHistoryLongClick
            )
            VIEW_TYPE_RESULT_HEADER -> ResultHeaderViewHolder(
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
            is SearchListItem.SearchBox -> (holder as SearchBoxViewHolder).bind(item)
            is SearchListItem.SearchHistorySection -> (holder as HotKeySectionViewHolder).bindHistory(item.history)
            is SearchListItem.HotKeySection -> (holder as HotKeySectionViewHolder).bind(item.hotKeys)
            is SearchListItem.ResultHeader -> (holder as ResultHeaderViewHolder).bind(item)
            is SearchListItem.ArticleRow -> (holder as ArticleViewHolder).bind(
                item.article,
                collectingArticleIds.contains(item.article.id)
            )
            is SearchListItem.LoadMoreFooter -> (holder as LoadMoreViewHolder).bind(item)
        }
    }

    private class SearchBoxViewHolder(
        itemView: View,
        private val onSearchSubmit: (String) -> Unit,
        private val onClearSearch: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val editText: EditText = itemView.findViewById(R.id.searchEditText)
        private val searchButton: Button = itemView.findViewById(R.id.searchButton)
        private val clearButton: Button = itemView.findViewById(R.id.clearSearchButton)

        init {
            searchButton.setOnClickListener { onSearchSubmit(editText.text.toString()) }
            clearButton.setOnClickListener {
                editText.setText("")
                onClearSearch()
            }
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onSearchSubmit(editText.text.toString())
                    true
                } else {
                    false
                }
            }
        }

        fun bind(item: SearchListItem.SearchBox) {
            if (editText.text.toString() != item.keyword) {
                editText.setText(item.keyword)
                editText.setSelection(editText.text.length)
            }
            clearButton.isVisible = item.isResultMode
            if (!item.isResultMode && editText.text.isNullOrBlank()) {
                editText.post { editText.requestFocus() }
            }
        }
    }

    private class HotKeySectionViewHolder(
        itemView: View,
        titleRes: Int,
        actionTextRes: Int?,
        onActionClick: (() -> Unit)?,
        private val onChipClick: (String) -> Unit,
        private val onChipLongClick: ((String) -> Unit)? = null
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.sectionTitle)
        private val actionView: TextView = itemView.findViewById(R.id.sectionAction)
        private val chipGroup: ChipGroup = itemView.findViewById(R.id.chipGroup)

        init {
            titleView.setText(titleRes)
            if (actionTextRes != null && onActionClick != null) {
                actionView.isVisible = true
                actionView.setText(actionTextRes)
                actionView.setOnClickListener { onActionClick() }
            } else {
                actionView.isVisible = false
                actionView.setOnClickListener(null)
            }
        }

        fun bind(hotKeys: List<HotKey>) {
            chipGroup.removeAllViews()
            hotKeys.forEach { hotKey ->
                val chip = Chip(itemView.context)
                chip.text = hotKey.name.ifBlank { hotKey.link }
                chip.isClickable = true
                chip.isCheckable = false
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.context, R.color.surface_tint)
                )
                chip.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
                chip.setOnClickListener { onChipClick(chip.text.toString()) }
                chipGroup.addView(chip)
            }
        }

        fun bindHistory(history: List<SearchHistoryItem>) {
            chipGroup.removeAllViews()
            history.forEach { item ->
                val chip = Chip(itemView.context)
                chip.text = item.keyword
                chip.isClickable = true
                chip.isCheckable = false
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.context, R.color.surface_tint)
                )
                chip.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
                chip.setOnClickListener { onChipClick(item.keyword) }
                chip.setOnLongClickListener {
                    onChipLongClick?.invoke(item.keyword)
                    true
                }
                chipGroup.addView(chip)
            }
        }
    }

    private class ResultHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.projectArticleTitle)
        private val countView: TextView = itemView.findViewById(R.id.projectArticleCount)

        fun bind(item: SearchListItem.ResultHeader) {
            titleView.text = itemView.context.getString(R.string.project_search_result_title, item.keyword)
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

        fun bind(item: SearchListItem.LoadMoreFooter) {
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
        private const val VIEW_TYPE_SEARCH_BOX = 1
        private const val VIEW_TYPE_SEARCH_HISTORY = 2
        private const val VIEW_TYPE_HOT_KEYS = 3
        private const val VIEW_TYPE_RESULT_HEADER = 4
        private const val VIEW_TYPE_ARTICLE = 5
        private const val VIEW_TYPE_LOAD_MORE = 6
    }
}
