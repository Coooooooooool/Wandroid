package com.eric.wandroid.ui.moyu.wallpaper

data class WallpaperUiState(
    val selectedGroupIndex: Int = 1,
    val selectedCategory: WallpaperCategory = WallpaperCategory("横屏壁纸", "pc"),
    val wallpapers: List<String> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)
