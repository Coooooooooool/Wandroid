package com.eric.wandroid.common.storage

import android.content.Context
import java.io.File
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

object CacheUtils {
    fun getTotalCacheSize(context: Context): Long {
        val internalSize = directorySize(context.cacheDir)
        val externalSize = context.externalCacheDirs
            ?.filterNotNull()
            ?.sumOf(::directorySize)
            .orZero()
        return internalSize + externalSize
    }

    fun clearAppCache(context: Context) {
        deleteChildren(context.cacheDir)
        context.externalCacheDirs
            ?.filterNotNull()
            ?.forEach(::deleteChildren)
    }

    fun formatSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroup = (ln(sizeInBytes.toDouble()) / ln(1024.0)).toInt().coerceAtMost(units.lastIndex)
        val scaled = sizeInBytes / 1024.0.pow(digitGroup.toDouble())
        return String.format(Locale.US, "%.1f %s", scaled, units[digitGroup])
    }

    private fun directorySize(file: File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        return file.listFiles().orEmpty().sumOf(::directorySize)
    }

    private fun deleteChildren(directory: File) {
        if (!directory.exists() || !directory.isDirectory) return
        directory.listFiles().orEmpty().forEach(::deleteRecursively)
    }

    private fun deleteRecursively(file: File) {
        if (file.isDirectory) {
            file.listFiles().orEmpty().forEach(::deleteRecursively)
        }
        file.delete()
    }

    private fun Long?.orZero(): Long = this ?: 0L
}
