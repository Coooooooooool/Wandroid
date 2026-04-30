package com.eric.wandroid.ui.moyu.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.RemoteImageLoader

class NewsContentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<NewsContentBlock>()

    fun submitItems(content: List<NewsContentBlock>) {
        items.clear()
        items += content
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is NewsContentBlock.Paragraph -> VIEW_TYPE_PARAGRAPH
        is NewsContentBlock.Image -> VIEW_TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_PARAGRAPH -> ParagraphViewHolder(
                inflater.inflate(R.layout.item_news_content_paragraph, parent, false)
            )

            VIEW_TYPE_IMAGE -> ImageViewHolder(
                inflater.inflate(R.layout.item_news_content_image, parent, false)
            )

            else -> error("Unsupported view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is NewsContentBlock.Paragraph -> (holder as ParagraphViewHolder).bind(item)
            is NewsContentBlock.Image -> (holder as ImageViewHolder).bind(item)
        }
    }

    private class ParagraphViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.newsParagraph)

        fun bind(item: NewsContentBlock.Paragraph) {
            textView.text = item.text
        }
    }

    private class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.newsContentImage)

        fun bind(item: NewsContentBlock.Image) {
            RemoteImageLoader.loadInto(imageView, item.url)
        }
    }

    companion object {
        private const val VIEW_TYPE_PARAGRAPH = 1
        private const val VIEW_TYPE_IMAGE = 2
    }
}
