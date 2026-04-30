package com.eric.wandroid.ui.moyu.news

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class NewsCategoryPagerAdapter(
    activity: AppCompatActivity,
    private val categories: List<NewsCategoryUiModel>
) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        return NewsCategoryPageFragment.newInstance(categories[position].type)
    }
}
