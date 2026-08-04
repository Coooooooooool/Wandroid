package com.eric.wandroid.ui.moyu.wallpaper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.eric.wandroid.R
import com.eric.wandroid.common.storage.WallpaperDownloader
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.eric.wandroid.common.ui.RemoteImageLoader
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class WallpaperPreviewActivity : AppCompatActivity() {
    private lateinit var imageUrl: String
    private lateinit var imageView: ImageView
    private lateinit var controls: View
    private lateinit var backButton: ImageButton
    private lateinit var downloadButton: ImageButton
    private lateinit var downloadProgress: View

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            downloadWallpaper()
        } else {
            Snackbar.make(imageView, R.string.wallpaper_storage_permission_required, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL).orEmpty()
        if (imageUrl.isBlank()) {
            finish()
            return
        }
        setContentView(R.layout.activity_wallpaper_preview)
        bindViews()
        EdgeToEdgeHelper.applyContentOnly(this, controls, imageView)
        backButton.setOnClickListener { finish() }
        downloadButton.setOnClickListener { requestDownload() }
        RemoteImageLoader.loadInto(imageView, imageUrl, ImageView.ScaleType.FIT_CENTER)
    }

    private fun bindViews() {
        imageView = findViewById(R.id.wallpaperPreviewImage)
        controls = findViewById(R.id.wallpaperPreviewControls)
        backButton = findViewById(R.id.wallpaperPreviewBack)
        downloadButton = findViewById(R.id.wallpaperDownloadButton)
        downloadProgress = findViewById(R.id.wallpaperDownloadProgress)
    }

    private fun requestDownload() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        downloadWallpaper()
    }

    private fun downloadWallpaper() {
        downloadButton.isEnabled = false
        downloadProgress.isVisible = true
        lifecycleScope.launch {
            when (val result = WallpaperDownloader.download(this@WallpaperPreviewActivity, imageUrl)) {
                is WallpaperDownloader.DownloadResult.Success -> {
                    Snackbar.make(imageView, R.string.wallpaper_download_success, Snackbar.LENGTH_LONG).show()
                }

                is WallpaperDownloader.DownloadResult.Error -> {
                    Snackbar.make(imageView, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
            downloadProgress.isVisible = false
            downloadButton.isEnabled = true
        }
    }

    companion object {
        private const val EXTRA_IMAGE_URL = "image_url"

        fun createIntent(context: Context, imageUrl: String): Intent {
            return Intent(context, WallpaperPreviewActivity::class.java)
                .putExtra(EXTRA_IMAGE_URL, imageUrl)
        }
    }
}
