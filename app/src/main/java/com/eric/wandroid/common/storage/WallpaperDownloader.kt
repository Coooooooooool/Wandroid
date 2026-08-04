package com.eric.wandroid.common.storage

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object WallpaperDownloader {
    private const val DIRECTORY_NAME = "Wandroid Wallpapers"
    private val httpClient = OkHttpClient()

    suspend fun download(context: Context, imageUrl: String): DownloadResult = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(imageUrl).build()
            httpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Download failed." }
                val body = checkNotNull(response.body) { "Empty image response." }
                val mimeType = response.header("Content-Type")?.substringBefore(';') ?: "image/jpeg"
                val displayName = "wallpaper_${System.currentTimeMillis()}.${extensionFor(mimeType, imageUrl)}"

                body.byteStream().use { input ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        saveWithMediaStore(context, input, displayName, mimeType)
                    } else {
                        saveToLegacyPictures(context, input, displayName)
                    }
                }
            }
        }.fold(
            onSuccess = { DownloadResult.Success(it) },
            onFailure = { DownloadResult.Error(it.message ?: "Download failed.") }
        )
    }

    private fun saveWithMediaStore(
        context: Context,
        input: java.io.InputStream,
        displayName: String,
        mimeType: String
    ): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/$DIRECTORY_NAME"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = checkNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)) {
            "Unable to create image file."
        }
        try {
            checkNotNull(resolver.openOutputStream(uri)).use { output -> input.copyTo(output) }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri
        } catch (exception: Throwable) {
            resolver.delete(uri, null, null)
            throw exception
        }
    }

    private fun saveToLegacyPictures(
        context: Context,
        input: java.io.InputStream,
        displayName: String
    ): Uri {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            DIRECTORY_NAME
        )
        check(directory.exists() || directory.mkdirs()) { "Unable to create image folder." }
        val file = File(directory, displayName)
        FileOutputStream(file).use { output -> input.copyTo(output) }
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
        return Uri.fromFile(file)
    }

    private fun extensionFor(mimeType: String, imageUrl: String): String {
        return when {
            mimeType.contains("png", ignoreCase = true) -> "png"
            mimeType.contains("webp", ignoreCase = true) -> "webp"
            mimeType.contains("gif", ignoreCase = true) -> "gif"
            else -> imageUrl.substringBefore('?').substringAfterLast('.', "jpg")
                .lowercase(Locale.US)
                .takeIf { it.length in 2..5 } ?: "jpg"
        }
    }

    sealed interface DownloadResult {
        data class Success(val uri: Uri) : DownloadResult
        data class Error(val message: String) : DownloadResult
    }
}
