package com.eric.wandroid.ui.system

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.domain.model.SystemRootCategory

class SystemRootListAdapter(
    private val onRootClick: (SystemRootCategory) -> Unit
) : RecyclerView.Adapter<SystemRootListAdapter.ViewHolder>() {
    private val items = mutableListOf<SystemRootCategory>()

    fun submitList(list: List<SystemRootCategory>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_system_root_card, parent, false)
        return ViewHolder(view, onRootClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        itemView: View,
        private val onRootClick: (SystemRootCategory) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.rootTitle)
        private val subtitleView: TextView = itemView.findViewById(R.id.rootSubtitle)
        private val childPreviewView: TextView = itemView.findViewById(R.id.rootChildPreview)

        fun bind(root: SystemRootCategory) {
            titleView.text = root.name
            subtitleView.text = itemView.context.getString(
                R.string.system_root_count_summary,
                root.children.size
            )
            childPreviewView.text = root.children.joinToString(" · ") { it.name }
            itemView.setOnClickListener { onRootClick(root) }
        }
    }
}
