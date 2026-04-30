package com.eric.wandroid.ui.message

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.domain.model.MessageOverview
import com.eric.wandroid.domain.model.UserMessage
import com.eric.wandroid.ui.web.WebContainerActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class MessageAdapter(
    private val onFilterSelected: (MessageFilter) -> Unit,
    private val onLoadMoreClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<MessageListItem>()

    fun submitState(state: MessageUiState) {
        items.clear()
        state.overview?.let { items += MessageListItem.Summary(it) }
        items += MessageListItem.FilterSection(state.selectedFilter)
        items += MessageListItem.Header(state.messages.size)
        items += state.messages.map { MessageListItem.MessageRow(it) }
        if (state.messages.isNotEmpty()) {
            items += MessageListItem.LoadMoreFooter(
                isLoading = state.isLoadingMore,
                canLoadMore = state.canLoadMore
            )
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is MessageListItem.Summary -> VIEW_TYPE_SUMMARY
        is MessageListItem.FilterSection -> VIEW_TYPE_FILTER
        is MessageListItem.Header -> VIEW_TYPE_HEADER
        is MessageListItem.MessageRow -> VIEW_TYPE_MESSAGE
        is MessageListItem.LoadMoreFooter -> VIEW_TYPE_LOAD_MORE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SUMMARY -> SummaryViewHolder(
                inflater.inflate(R.layout.item_message_summary, parent, false)
            )
            VIEW_TYPE_FILTER -> FilterViewHolder(
                inflater.inflate(R.layout.item_extra_chip_section, parent, false),
                onFilterSelected
            )
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_system_article_header, parent, false)
            )
            VIEW_TYPE_MESSAGE -> MessageViewHolder(
                inflater.inflate(R.layout.item_message_row, parent, false)
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
            is MessageListItem.Summary -> (holder as SummaryViewHolder).bind(item.overview)
            is MessageListItem.FilterSection -> (holder as FilterViewHolder).bind(item.selectedFilter)
            is MessageListItem.Header -> (holder as HeaderViewHolder).bind(item.count)
            is MessageListItem.MessageRow -> (holder as MessageViewHolder).bind(item.message)
            is MessageListItem.LoadMoreFooter -> (holder as LoadMoreViewHolder).bind(item)
        }
    }

    private class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val unreadCountView: TextView = itemView.findViewById(R.id.messageUnreadCount)
        private val summaryView: TextView = itemView.findViewById(R.id.messageSummaryText)

        fun bind(overview: MessageOverview) {
            unreadCountView.text = overview.unreadCount.toString()
            summaryView.text = itemView.context.getString(R.string.message_unread_summary, overview.unreadCount)
        }
    }

    private class FilterViewHolder(
        itemView: View,
        private val onFilterSelected: (MessageFilter) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.sectionTitle)
        private val chipGroup: ChipGroup = itemView.findViewById(R.id.chipGroup)

        fun bind(selectedFilter: MessageFilter) {
            titleView.setText(R.string.message_filter_title)
            chipGroup.removeAllViews()
            MessageFilter.entries.forEach { filter ->
                val chip = Chip(itemView.context).apply {
                    text = itemView.context.getString(
                        if (filter == MessageFilter.Unread) {
                            R.string.message_filter_unread
                        } else {
                            R.string.message_filter_read
                        }
                    )
                    isCheckable = true
                    isChecked = filter == selectedFilter
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
                    setOnClickListener { onFilterSelected(filter) }
                }
                chipGroup.addView(chip)
            }
        }
    }

    private class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.systemArticleTitle)
        private val subtitleView: TextView = itemView.findViewById(R.id.systemArticleSubtitle)

        fun bind(count: Int) {
            titleView.setText(R.string.message_list_title)
            subtitleView.text = itemView.context.getString(R.string.article_section_count, count)
        }
    }

    private class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView as MaterialCardView
        private val tagView: TextView = itemView.findViewById(R.id.messageTag)
        private val titleView: TextView = itemView.findViewById(R.id.messageTitle)
        private val contentView: TextView = itemView.findViewById(R.id.messageContent)
        private val metaView: TextView = itemView.findViewById(R.id.messageMeta)

        fun bind(message: UserMessage) {
            tagView.isVisible = message.tag.isNotBlank()
            tagView.text = message.tag
            titleView.text = message.title.ifBlank {
                itemView.context.getString(R.string.message_default_title)
            }
            contentView.text = message.content.ifBlank {
                itemView.context.getString(R.string.message_empty_content)
            }
            metaView.text = buildString {
                append(
                    if (message.isRead) {
                        itemView.context.getString(R.string.message_read_status)
                    } else {
                        itemView.context.getString(R.string.message_unread_status)
                    }
                )
                if (message.fromUser.isNotBlank()) {
                    append(" · ")
                    append(message.fromUser)
                }
                if (message.niceDate.isNotBlank()) {
                    append(" · ")
                    append(message.niceDate)
                }
            }
            cardView.strokeColor = ContextCompat.getColor(
                itemView.context,
                if (message.isRead) R.color.divider else R.color.brand_secondary
            )
            itemView.setOnClickListener {
                if (message.link.isNotBlank()) {
                    itemView.context.startActivity(
                        WebContainerActivity.createIntent(itemView.context, message.title, message.link)
                    )
                }
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

        fun bind(item: MessageListItem.LoadMoreFooter) {
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
        private const val VIEW_TYPE_SUMMARY = 1
        private const val VIEW_TYPE_FILTER = 2
        private const val VIEW_TYPE_HEADER = 3
        private const val VIEW_TYPE_MESSAGE = 4
        private const val VIEW_TYPE_LOAD_MORE = 5
    }
}
