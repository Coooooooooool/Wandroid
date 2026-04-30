package com.eric.wandroid.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.domain.model.ReadingHistoryItem
import com.eric.wandroid.ui.web.WebContainerActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReadingHistoryAdapter : RecyclerView.Adapter<ReadingHistoryAdapter.ViewHolder>() {
    private val items = mutableListOf<ReadingHistoryItem>()
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun submitList(list: List<ReadingHistoryItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reading_history, parent, false)
        return ViewHolder(view, formatter)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        itemView: View,
        private val formatter: SimpleDateFormat
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.historyTitle)
        private val metaView: TextView = itemView.findViewById(R.id.historyMeta)

        fun bind(item: ReadingHistoryItem) {
            titleView.text = HtmlCompat.fromHtml(item.title, HtmlCompat.FROM_HTML_MODE_LEGACY)
            metaView.text = itemView.context.getString(
                R.string.reading_history_meta,
                formatter.format(Date(item.timestamp))
            )
            itemView.setOnClickListener {
                itemView.context.startActivity(
                    WebContainerActivity.createIntent(itemView.context, titleView.text.toString(), item.url)
                )
            }
        }
    }
}
