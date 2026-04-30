package com.eric.wandroid.ui.coin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.domain.model.CoinOverview
import com.eric.wandroid.domain.model.CoinRecord
import java.text.DateFormat
import java.util.Date

class PointsAdapter(
    private val onLoadMoreClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<PointsListItem>()

    fun submitState(state: PointsUiState) {
        items.clear()
        state.overview?.let { items += PointsListItem.Summary(it) }
        items += PointsListItem.RecordsHeader(state.records.size)
        items += state.records.map { PointsListItem.RecordRow(it) }
        if (state.records.isNotEmpty()) {
            items += PointsListItem.LoadMoreFooter(
                isLoading = state.isLoadingMore,
                canLoadMore = state.canLoadMore
            )
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is PointsListItem.Summary -> VIEW_TYPE_SUMMARY
        is PointsListItem.RecordsHeader -> VIEW_TYPE_HEADER
        is PointsListItem.RecordRow -> VIEW_TYPE_RECORD
        is PointsListItem.LoadMoreFooter -> VIEW_TYPE_LOAD_MORE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SUMMARY -> SummaryViewHolder(
                inflater.inflate(R.layout.item_points_summary, parent, false)
            )
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_system_article_header, parent, false)
            )
            VIEW_TYPE_RECORD -> RecordViewHolder(
                inflater.inflate(R.layout.item_coin_record, parent, false)
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
            is PointsListItem.Summary -> (holder as SummaryViewHolder).bind(item.overview)
            is PointsListItem.RecordsHeader -> (holder as HeaderViewHolder).bind(item.count)
            is PointsListItem.RecordRow -> (holder as RecordViewHolder).bind(item.record)
            is PointsListItem.LoadMoreFooter -> (holder as LoadMoreViewHolder).bind(item)
        }
    }

    private class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val displayNameView: TextView = itemView.findViewById(R.id.pointsDisplayName)
        private val usernameView: TextView = itemView.findViewById(R.id.pointsUsername)
        private val totalView: TextView = itemView.findViewById(R.id.pointsTotal)
        private val rankView: TextView = itemView.findViewById(R.id.pointsRank)

        fun bind(overview: CoinOverview) {
            displayNameView.text = overview.displayName
            usernameView.text = itemView.context.getString(R.string.points_username_value_cn, overview.username)
            totalView.text = itemView.context.getString(R.string.points_total_value_cn, overview.coinCount)
            rankView.text = itemView.context.getString(R.string.points_rank_value_cn, overview.rank)
        }
    }

    private class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.systemArticleTitle)
        private val subtitleView: TextView = itemView.findViewById(R.id.systemArticleSubtitle)

        fun bind(count: Int) {
            titleView.setText(R.string.points_history_title_cn)
            subtitleView.text = itemView.context.getString(R.string.article_section_count, count)
        }
    }

    private class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.coinRecordTitle)
        private val descView: TextView = itemView.findViewById(R.id.coinRecordDescription)
        private val metaView: TextView = itemView.findViewById(R.id.coinRecordMeta)
        private val valueView: TextView = itemView.findViewById(R.id.coinRecordValue)

        fun bind(record: CoinRecord) {
            titleView.text = record.reason.ifBlank {
                itemView.context.getString(R.string.points_record_default_reason_cn)
            }
            descView.text = record.description.ifBlank {
                itemView.context.getString(R.string.points_record_default_description_cn)
            }
            metaView.text = formatDate(record.date)
            valueView.text = formatCoinDelta(record.coinCount)
        }

        private fun formatDate(timestamp: Long): String {
            if (timestamp <= 0L) {
                return itemView.context.getString(R.string.unknown_time)
            }
            return DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT
            ).format(Date(timestamp))
        }

        private fun formatCoinDelta(coinCount: Int): String {
            return if (coinCount > 0) {
                "+$coinCount"
            } else {
                coinCount.toString()
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

        fun bind(item: PointsListItem.LoadMoreFooter) {
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
        private const val VIEW_TYPE_HEADER = 2
        private const val VIEW_TYPE_RECORD = 3
        private const val VIEW_TYPE_LOAD_MORE = 4
    }
}
