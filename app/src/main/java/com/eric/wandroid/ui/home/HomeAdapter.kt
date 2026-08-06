package com.eric.wandroid.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.eric.wandroid.R
import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.Banner
import com.eric.wandroid.ui.web.WebContainerActivity
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import android.graphics.drawable.GradientDrawable

class HomeAdapter(
    private val onSearchClick: () -> Unit,
    private val onLoadMoreClick: () -> Unit,
    private val onCollectClick: (Article) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<HomeListItem>()
    private var collectingArticleIds: Set<Int> = emptySet()
    private var bannerAutoScrollEnabled: Boolean = true

    fun submitState(state: HomeUiState) {
        items.clear()
        collectingArticleIds = state.collectingArticleIds
        items += HomeListItem.SearchEntry
        if (state.banners.isNotEmpty()) {
            items += HomeListItem.BannerSection(state.banners)
        }
        if (state.popularRoutes.isNotEmpty() || state.popularWenda.isNotEmpty() || state.popularColumns.isNotEmpty()) {
            items += HomeListItem.PopularSection(
                routes = state.popularRoutes,
                wenda = state.popularWenda,
                columns = state.popularColumns
            )
        }
        if (state.articles.isNotEmpty()) {
            items += HomeListItem.ArticleHeader(state.articles.size)
            items += state.articles.map { HomeListItem.ArticleRow(it) }
            items += HomeListItem.LoadMoreFooter(
                isLoading = state.isLoadingMore,
                canLoadMore = state.canLoadMore
            )
        }
        notifyDataSetChanged()
    }

    fun setBannerAutoScrollEnabled(enabled: Boolean) {
        if (bannerAutoScrollEnabled == enabled) return
        bannerAutoScrollEnabled = enabled
        val bannerIndex = items.indexOfFirst { it is HomeListItem.BannerSection }
        if (bannerIndex >= 0) {
            notifyItemChanged(bannerIndex)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        HomeListItem.SearchEntry -> VIEW_TYPE_SEARCH_ENTRY
        is HomeListItem.BannerSection -> VIEW_TYPE_BANNERS
        is HomeListItem.PopularSection -> VIEW_TYPE_POPULAR
        is HomeListItem.ArticleHeader -> VIEW_TYPE_ARTICLE_HEADER
        is HomeListItem.ArticleRow -> VIEW_TYPE_ARTICLE
        is HomeListItem.LoadMoreFooter -> VIEW_TYPE_LOAD_MORE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SEARCH_ENTRY -> SearchEntryViewHolder(
                inflater.inflate(R.layout.item_home_search_entry, parent, false),
                onSearchClick
            )
            VIEW_TYPE_BANNERS -> BannerSectionViewHolder(
                inflater.inflate(R.layout.item_home_banner_section, parent, false)
            )
            VIEW_TYPE_POPULAR -> PopularSectionViewHolder(
                inflater.inflate(R.layout.item_home_popular_section, parent, false)
            )
            VIEW_TYPE_ARTICLE_HEADER -> ArticleHeaderViewHolder(
                inflater.inflate(R.layout.item_home_article_header, parent, false)
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
            HomeListItem.SearchEntry -> (holder as SearchEntryViewHolder).bind()
            is HomeListItem.BannerSection -> (holder as BannerSectionViewHolder).bind(item.banners, bannerAutoScrollEnabled)
            is HomeListItem.PopularSection -> (holder as PopularSectionViewHolder).bind(item)
            is HomeListItem.ArticleHeader -> (holder as ArticleHeaderViewHolder).bind(item.count)
            is HomeListItem.ArticleRow -> (holder as ArticleViewHolder).bind(
                article = item.article,
                isCollecting = collectingArticleIds.contains(item.article.id)
            )
            is HomeListItem.LoadMoreFooter -> (holder as LoadMoreViewHolder).bind(item)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is BannerSectionViewHolder) {
            holder.release()
        }
        super.onViewRecycled(holder)
    }

    private class SearchEntryViewHolder(
        itemView: View,
        private val onSearchClick: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.searchIcon)
        private val hintView: TextView = itemView.findViewById(R.id.searchHint)

        init {
            itemView.setOnClickListener { onSearchClick() }
        }

        fun bind() {
            hintView.setText(R.string.home_search_entry_hint)
            iconView.contentDescription = itemView.context.getString(R.string.home_search_entry_hint)
        }
    }

    private class BannerSectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewPager: ViewPager2 = itemView.findViewById(R.id.bannerPager)
        private val indicator: TabLayout = itemView.findViewById(R.id.bannerIndicator)
        private val pagerAdapter = HomeBannerPagerAdapter { banner ->
            openWebPage(
                title = banner.title.ifBlank { banner.url },
                url = banner.url
            )
        }
        private val density = itemView.resources.displayMetrics.density
        private var mediator: TabLayoutMediator? = null
        private var currentBannerCount: Int = 0
        private var autoScrollEnabled: Boolean = true
        private val indicatorSelectionListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateIndicatorDots(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                updateIndicatorDots(indicator.selectedTabPosition)
            }

            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        }
        private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    ViewPager2.SCROLL_STATE_DRAGGING -> stopAutoScroll()
                    ViewPager2.SCROLL_STATE_IDLE -> scheduleAutoScroll(AUTO_SCROLL_RESUME_DELAY_MS)
                }
            }
        }
        private val autoScrollRunnable = object : Runnable {
            override fun run() {
                val count = currentBannerCount
                if (!autoScrollEnabled || count <= 1) return
                val nextItem = (viewPager.currentItem + 1) % count
                viewPager.setCurrentItem(nextItem, true)
                viewPager.postDelayed(this, AUTO_SCROLL_INTERVAL_MS)
            }
        }

        init {
            viewPager.adapter = pagerAdapter
            viewPager.registerOnPageChangeCallback(pageChangeCallback)
            indicator.addOnTabSelectedListener(indicatorSelectionListener)
        }

        fun bind(banners: List<Banner>, autoScrollEnabled: Boolean) {
            this.autoScrollEnabled = autoScrollEnabled
            currentBannerCount = banners.size
            pagerAdapter.submitItems(banners)
            mediator?.detach()
            mediator = TabLayoutMediator(indicator, viewPager) { tab, _ ->
                tab.customView = createIndicatorDot()
            }
            mediator?.attach()
            indicator.isVisible = banners.size > 1
            stopAutoScroll()
            if (viewPager.currentItem >= banners.size) {
                viewPager.setCurrentItem(0, false)
            }
            updateIndicatorDots(viewPager.currentItem)
            scheduleAutoScroll(AUTO_SCROLL_INTERVAL_MS)
        }

        private fun createIndicatorDot(): View {
            return View(itemView.context).apply {
                layoutParams = ViewGroup.MarginLayoutParams((14 * density).toInt(), (3 * density).toInt()).also {
                    it.marginStart = (3 * density).toInt()
                    it.marginEnd = (3 * density).toInt()
                }
            }
        }

        private fun updateIndicatorDots(selectedPosition: Int) {
            val safeSelected = selectedPosition.coerceAtLeast(0)
            for (index in 0 until indicator.tabCount) {
                val dotView = indicator.getTabAt(index)?.customView ?: continue
                val isSelected = index == safeSelected
                dotView.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 99f
                    setColor(
                        ContextCompat.getColor(
                            itemView.context,
                            if (isSelected) R.color.brand_primary else R.color.divider
                        )
                    )
                }
                dotView.layoutParams = (dotView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    width = if (isSelected) (14 * density).toInt() else (8 * density).toInt()
                    height = (3 * density).toInt()
                }
                dotView.requestLayout()
            }
        }

        private fun openWebPage(title: String, url: String) {
            itemView.context.startActivity(WebContainerActivity.createIntent(itemView.context, title, url))
        }

        private fun stopAutoScroll() {
            viewPager.removeCallbacks(autoScrollRunnable)
        }

        private fun scheduleAutoScroll(delayMillis: Long) {
            stopAutoScroll()
            if (!autoScrollEnabled || currentBannerCount <= 1) return
            viewPager.postDelayed(autoScrollRunnable, delayMillis)
        }

        fun release() {
            stopAutoScroll()
            mediator?.detach()
            mediator = null
        }

        companion object {
            private const val AUTO_SCROLL_INTERVAL_MS = 4_000L
            private const val AUTO_SCROLL_RESUME_DELAY_MS = 5_500L
        }
    }

    private class PopularSectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.popularCardRecyclerView)
        private val adapter = HomePopularCardAdapter()

        init {
            recyclerView.layoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
            recyclerView.adapter = adapter
            if (recyclerView.itemDecorationCount == 0) {
                val density = itemView.resources.displayMetrics.density
                recyclerView.addItemDecoration(
                    HomePopularCardAdapter.CarouselSpacingDecoration(
                        leading = (2 * density).toInt(),
                        between = (12 * density).toInt(),
                        trailing = (56 * density).toInt()
                    )
                )
            }
        }

        fun bind(item: HomeListItem.PopularSection) {
            adapter.submitData(
                routes = item.routes,
                wenda = item.wenda,
                columns = item.columns
            )
        }
    }

    private class ArticleHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.sectionTitle)
        private val countView: TextView = itemView.findViewById(R.id.sectionCount)

        fun bind(count: Int) {
            titleView.setText(R.string.section_articles)
            countView.text = itemView.context.getString(R.string.article_section_count, count)
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
            topChip.isVisible = article.isTopPinned
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

        fun bind(item: HomeListItem.LoadMoreFooter) {
            progressBar.isVisible = item.isLoading
            button.isEnabled = item.canLoadMore && !item.isLoading
            button.text = when {
                item.isLoading -> itemView.context.getString(R.string.loading_home)
                item.canLoadMore -> itemView.context.getString(R.string.label_load_more)
                else -> itemView.context.getString(R.string.label_no_more)
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_SEARCH_ENTRY = 1
        private const val VIEW_TYPE_BANNERS = 2
        private const val VIEW_TYPE_POPULAR = 3
        private const val VIEW_TYPE_ARTICLE_HEADER = 4
        private const val VIEW_TYPE_ARTICLE = 5
        private const val VIEW_TYPE_LOAD_MORE = 6
    }
}
