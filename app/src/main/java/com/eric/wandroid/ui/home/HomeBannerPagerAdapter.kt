package com.eric.wandroid.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.RemoteImageLoader
import com.eric.wandroid.domain.model.Banner

class HomeBannerPagerAdapter(
    private val onBannerClick: (Banner) -> Unit
) : RecyclerView.Adapter<HomeBannerPagerAdapter.BannerViewHolder>() {
    private val items = mutableListOf<Banner>()

    fun submitItems(banners: List<Banner>) {
        items.clear()
        items.addAll(banners)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner_pager_card, parent, false)
        return BannerViewHolder(view, onBannerClick)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class BannerViewHolder(
        itemView: View,
        private val onBannerClick: (Banner) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.bannerImageView)

        fun bind(banner: Banner) {
            imageView.contentDescription = banner.title.ifBlank { itemView.context.getString(R.string.section_banners) }
            RemoteImageLoader.loadInto(imageView, banner.imagePath)
            itemView.setOnClickListener { onBannerClick(banner) }
        }
    }
}
