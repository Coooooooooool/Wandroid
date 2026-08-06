package com.eric.wandroid.ui.home

import android.content.Intent
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.PopularColumn
import com.eric.wandroid.domain.model.PopularRoute
import com.eric.wandroid.data.repository.ExtraContentMode
import com.eric.wandroid.ui.extra.ExtraContentActivity
import com.eric.wandroid.ui.system.SystemRootListActivity
import com.eric.wandroid.ui.system.SystemContentMode
import com.eric.wandroid.ui.system.SystemNavigationActivity
import com.eric.wandroid.ui.wenda.WendaDetailActivity
import com.eric.wandroid.ui.web.WebContainerActivity

class HomePopularCardAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<HomePopularCard>()

    fun submitData(
        routes: List<PopularRoute>,
        wenda: List<Article>,
        columns: List<PopularColumn>
    ) {
        items.clear()
        if (routes.isEmpty() && wenda.isEmpty() && columns.isEmpty()) {
            notifyDataSetChanged()
            return
        }
        if (routes.isNotEmpty()) items += HomePopularCard.RouteFocus(routes.take(6))
        if (wenda.isNotEmpty()) items += HomePopularCard.WendaFocus(wenda.take(6))
        if (columns.isNotEmpty()) items += HomePopularCard.ColumnFocus(columns.take(6))
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is HomePopularCard.RouteFocus -> VIEW_TYPE_ROUTE
        is HomePopularCard.WendaFocus -> VIEW_TYPE_WENDA
        is HomePopularCard.ColumnFocus -> VIEW_TYPE_COLUMN
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_ROUTE, VIEW_TYPE_WENDA, VIEW_TYPE_COLUMN -> FocusViewHolder(
                inflater.inflate(R.layout.item_home_popular_focus_card, parent, false)
            )
            else -> error("Unsupported view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomePopularCard.RouteFocus -> (holder as FocusViewHolder).bindRoutes(item.routes)
            is HomePopularCard.WendaFocus -> (holder as FocusViewHolder).bindWenda(item.wenda)
            is HomePopularCard.ColumnFocus -> (holder as FocusViewHolder).bindColumns(item.columns)
        }
    }

    class CarouselSpacingDecoration(
        private val leading: Int,
        private val between: Int,
        private val trailing: Int
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return
            outRect.left = if (position == 0) leading else between
            outRect.right = if (position == state.itemCount - 1) trailing else 0
        }
    }

    private class FocusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.cardTitle)
        private val actionView: TextView = itemView.findViewById(R.id.cardAction)
        private val itemContainer: ViewGroup = itemView.findViewById(R.id.cardItems)

        fun bindRoutes(routes: List<PopularRoute>) {
            titleView.setText(R.string.popular_route_title)
            actionView.setText(R.string.popular_more_action)
            actionView.setOnClickListener {
                itemView.context.startActivity(
                    SystemRootListActivity.createIntent(itemView.context)
                )
            }
            bindTextList(itemContainer, routes.map { it.name }) { _ ->
                itemView.context.startActivity(
                    SystemRootListActivity.createIntent(itemView.context)
                )
            }
        }

        fun bindWenda(wenda: List<Article>) {
            titleView.setText(R.string.popular_wenda_title)
            actionView.setText(R.string.popular_more_action)
            actionView.setOnClickListener {
                itemView.context.startActivity(
                    ExtraContentActivity.createIntent(itemView.context, ExtraContentMode.Wenda)
                )
            }
            bindTextList(itemContainer, wenda.map { renderTitle(it.title) }) { index ->
                val article = wenda[index]
                val intent = if (article.id > 0) {
                    WendaDetailActivity.createIntent(itemView.context, article)
                } else {
                    WebContainerActivity.createArticleIntent(itemView.context, renderTitle(article.title), article.link)
                }
                itemView.context.startActivity(intent)
            }
        }

        fun bindColumns(columns: List<PopularColumn>) {
            titleView.setText(R.string.popular_column_title)
            actionView.setText(R.string.popular_subscribe_action)
            actionView.setOnClickListener {
                itemView.context.startActivity(
                    ExtraContentActivity.createIntent(itemView.context, ExtraContentMode.Wechat)
                )
            }
            bindTextList(itemContainer, columns.map { it.name }) { index ->
                val column = columns[index]
                if (column.url.isNotBlank()) {
                    itemView.context.startActivity(
                        WebContainerActivity.createIntent(itemView.context, column.name, column.url)
                    )
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_ROUTE = 1
        private const val VIEW_TYPE_WENDA = 2
        private const val VIEW_TYPE_COLUMN = 3

        private fun renderTitle(rawTitle: String): String {
            return HtmlCompat.fromHtml(rawTitle, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        }

        private fun bindTextList(
            container: ViewGroup,
            items: List<String>,
            onItemClick: (Int) -> Unit
        ) {
            container.removeAllViews()
            val inflater = LayoutInflater.from(container.context)
            items.take(6).forEachIndexed { index, label ->
                val textView = inflater.inflate(R.layout.item_home_popular_text, container, false) as TextView
                textView.text = label
                textView.setOnClickListener { onItemClick(index) }
                container.addView(textView)
            }
            if (items.isEmpty()) {
                val emptyView = inflater.inflate(R.layout.item_home_popular_text, container, false) as TextView
                emptyView.text = container.context.getString(R.string.popular_empty)
                emptyView.isClickable = false
                emptyView.setTextColor(ContextCompat.getColor(container.context, R.color.text_secondary))
                container.addView(emptyView)
            }
        }
    }
}
