package com.eric.wandroid.ui.moyu

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.eric.wandroid.R

class MoyuVideoPagerAdapter : RecyclerView.Adapter<MoyuVideoPagerAdapter.VideoViewHolder>() {
    private val items = mutableListOf<String>()
    private var activePosition: Int = RecyclerView.NO_POSITION

    fun submitItems(videoUrls: List<String>, activePosition: Int) {
        items.clear()
        items.addAll(videoUrls)
        this.activePosition = activePosition
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_moyu_video_page, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(items[position], position == activePosition)
    }

    override fun getItemCount(): Int = items.size

    override fun onViewRecycled(holder: VideoViewHolder) {
        holder.release()
        super.onViewRecycled(holder)
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val videoContainer: FrameLayout = itemView.findViewById(R.id.pageVideoContainer)
        private val videoView: VideoView = itemView.findViewById(R.id.pageVideoView)
        private val loadingView: ProgressBar = itemView.findViewById(R.id.pageVideoLoading)
        private val hintView: TextView = itemView.findViewById(R.id.pageVideoHint)
        private var currentUrl: String = ""
        private var videoWidth: Int = 0
        private var videoHeight: Int = 0

        fun bind(videoUrl: String, shouldPlay: Boolean) {
            hintView.setText(R.string.moyu_video_swipe_hint)
            if (currentUrl != videoUrl) {
                currentUrl = videoUrl
                videoWidth = 0
                videoHeight = 0
                loadingView.visibility = View.VISIBLE
                videoView.background = ColorDrawable(Color.TRANSPARENT)
                videoView.setVideoPath(videoUrl)
                videoView.setOnPreparedListener { mediaPlayer ->
                    loadingView.visibility = View.GONE
                    mediaPlayer.isLooping = true
                    videoWidth = mediaPlayer.videoWidth
                    videoHeight = mediaPlayer.videoHeight
                    updateVideoBounds()
                    if (shouldPlay) {
                        mediaPlayer.start()
                    }
                }
            }
            updateVideoBounds()
            if (shouldPlay) {
                videoView.start()
            } else {
                videoView.pause()
            }
        }

        fun release() {
            videoView.stopPlayback()
            currentUrl = ""
            videoWidth = 0
            videoHeight = 0
        }

        private fun updateVideoBounds() {
            videoContainer.post {
                val containerWidth = videoContainer.width
                val containerHeight = videoContainer.height
                if (containerWidth <= 0 || containerHeight <= 0 || videoWidth <= 0 || videoHeight <= 0) {
                    videoView.layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                    )
                    return@post
                }

                val containerRatio = containerWidth.toFloat() / containerHeight.toFloat()
                val videoRatio = videoWidth.toFloat() / videoHeight.toFloat()
                val targetWidth: Int
                val targetHeight: Int
                if (videoRatio > containerRatio) {
                    targetWidth = containerWidth
                    targetHeight = (containerWidth / videoRatio).toInt()
                } else {
                    targetHeight = containerHeight
                    targetWidth = (containerHeight * videoRatio).toInt()
                }

                videoView.layoutParams = FrameLayout.LayoutParams(
                    targetWidth,
                    targetHeight,
                    Gravity.CENTER
                )
            }
        }
    }
}
