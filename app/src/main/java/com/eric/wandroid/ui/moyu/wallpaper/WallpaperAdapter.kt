package com.eric.wandroid.ui.moyu.wallpaper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.RemoteImageLoader
import com.google.android.material.card.MaterialCardView

class WallpaperAdapter(
    private val onWallpaperClick: (String) -> Unit
) : RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder>() {
    private var urls: List<String> = emptyList()

    fun submitUrls(newUrls: List<String>) {
        urls = newUrls
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder {
        return WallpaperViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_wallpaper, parent, false),
            onWallpaperClick
        )
    }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        holder.bind(urls[position])
    }

    override fun getItemCount(): Int = urls.size

    class WallpaperViewHolder(
        itemView: View,
        private val onWallpaperClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.wallpaperCard)
        private val image: ImageView = itemView.findViewById(R.id.wallpaperImage)

        fun bind(url: String) {
            val density = itemView.resources.displayMetrics.density
            val height = (190 + Math.floorMod(url.hashCode(), 150)) * density
            image.layoutParams = image.layoutParams.apply { this.height = height.toInt() }
            RemoteImageLoader.loadInto(image, url)
            itemView.setOnClickListener { onWallpaperClick(url) }
        }
    }
}
