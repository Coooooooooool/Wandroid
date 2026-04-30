package com.eric.wandroid.ui.moyu.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class NewsListActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var categoryTabs: TabLayout
    private lateinit var viewPager: ViewPager2

    private val categories = defaultNewsCategories()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_list)
        bindViews()
        EdgeToEdgeHelper.applySurfaceToolbar(this, toolbar, categoryTabs, viewPager)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = "摸鱼新闻"
        toolbar.subtitle = null

        viewPager.adapter = NewsCategoryPagerAdapter(this, categories)
        viewPager.offscreenPageLimit = 1
        TabLayoutMediator(categoryTabs, viewPager) { tab, position ->
            tab.text = categories[position].label
        }.attach()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        categoryTabs = findViewById(R.id.newsCategoryTabs)
        viewPager = findViewById(R.id.newsViewPager)
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, NewsListActivity::class.java)
        }
    }
}
