package com.eric.wandroid.common.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.collection.LruCache
import androidx.core.content.ContextCompat
import com.eric.wandroid.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object RemoteImageLoader {
    private val memoryCache = object : LruCache<String, Bitmap>(20 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    private val imageScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val httpClient = OkHttpClient()

    fun loadInto(imageView: ImageView, url: String) {
        imageView.tag = url
        imageView.setImageDrawable(
            ContextCompat.getDrawable(imageView.context, R.drawable.bg_banner_placeholder)
        )
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        if (url.isBlank()) return

        memoryCache.get(url)?.let { cachedBitmap ->
            imageView.setImageBitmap(cachedBitmap)
            return
        }

        imageScope.launch {
            val bitmap = withContext(Dispatchers.IO) { fetchBitmap(url) }
            if (imageView.tag != url) return@launch
            if (bitmap == null) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(imageView.context, R.drawable.bg_banner_error_placeholder)
                )
                imageView.scaleType = ImageView.ScaleType.CENTER
                return@launch
            }
            memoryCache.put(url, bitmap)
            imageView.setImageBitmap(bitmap)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    fun clearMemoryCache() {
        memoryCache.evictAll()
    }

    private fun fetchBitmap(url: String): Bitmap? {
        return runCatching {
            val request = Request.Builder()
                .url(url)
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body ?: return null
                body.byteStream().use(BitmapFactory::decodeStream)
            }
        }.getOrNull()
    }
}
