package com.eric.wandroid.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eric.wandroid.R
import com.eric.wandroid.common.ui.EdgeToEdgeHelper
import com.eric.wandroid.ui.shell.DiscoverFragment
import com.eric.wandroid.ui.shell.HomeFeedFragment
import com.eric.wandroid.ui.shell.MainTab
import com.eric.wandroid.ui.shell.MyFragment
import com.eric.wandroid.ui.shell.ProjectsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private lateinit var mainContentContainer: androidx.fragment.app.FragmentContainerView
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        bindViews()
        EdgeToEdgeHelper.applyContentOnly(this, mainContentContainer, bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { item ->
            val tab = when (item.itemId) {
                R.id.navigation_home -> MainTab.Home
                R.id.navigation_discover -> MainTab.Discover
                R.id.navigation_projects -> MainTab.Projects
                R.id.navigation_my -> MainTab.My
                else -> return@setOnItemSelectedListener false
            }
            showTab(tab)
            true
        }

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = tabToItemId(resolveInitialTab())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        bottomNavigationView.selectedItemId = tabToItemId(resolveInitialTab())
    }

    private fun bindViews() {
        mainContentContainer = findViewById(R.id.mainContentContainer)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
    }

    private fun resolveInitialTab(): MainTab {
        val rawTab = intent.getStringExtra(EXTRA_INITIAL_TAB) ?: return MainTab.Home
        return MainTab.entries.firstOrNull { it.name == rawTab } ?: MainTab.Home
    }

    private fun tabToItemId(tab: MainTab): Int {
        return when (tab) {
            MainTab.Home -> R.id.navigation_home
            MainTab.Discover -> R.id.navigation_discover
            MainTab.Projects -> R.id.navigation_projects
            MainTab.My -> R.id.navigation_my
        }
    }

    private fun showTab(tab: MainTab) {
        val fragment = when (tab) {
            MainTab.Home -> HomeFeedFragment()
            MainTab.Discover -> DiscoverFragment()
            MainTab.Projects -> ProjectsFragment()
            MainTab.My -> MyFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContentContainer, fragment)
            .commit()
    }

    companion object {
        private const val EXTRA_INITIAL_TAB = "initial_tab"

        fun createIntent(context: Context, initialTab: MainTab = MainTab.Home): Intent {
            return Intent(context, HomeActivity::class.java)
                .putExtra(EXTRA_INITIAL_TAB, initialTab.name)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }
}
