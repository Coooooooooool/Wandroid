package com.eric.wandroid.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
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
    private val tabFragments = mutableMapOf<MainTab, Fragment>()
    private var currentTab: MainTab? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        bindViews()
        EdgeToEdgeHelper.applyContentOnly(this, mainContentContainer, bottomNavigationView)

        val initialTab = if (savedInstanceState == null) {
            resolveInitialTab()
        } else {
            tabFromItemId(bottomNavigationView.selectedItemId)
        }
        bottomNavigationView.selectedItemId = tabToItemId(initialTab)
        showTab(initialTab)

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
        bottomNavigationView.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.navigation_home) {
                (fragmentForIfCreated(MainTab.Home) as? HomeFeedFragment)
                    ?.refreshFromTabReselection()
            }
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
        if (supportFragmentManager.isStateSaved) return

        val fragment = fragmentFor(tab)
        if (currentTab == tab && fragment.isAdded) return

        val transaction = supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)

        supportFragmentManager.fragments
            .filter { it.tag?.startsWith(FRAGMENT_TAG_PREFIX) == true && it != fragment }
            .forEach { existing ->
                transaction.hide(existing)
                transaction.setMaxLifecycle(existing, Lifecycle.State.STARTED)
            }

        if (fragment.isAdded) {
            transaction.show(fragment)
        } else {
            transaction.add(R.id.mainContentContainer, fragment, fragmentTag(tab))
        }
        transaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
        transaction.commit()
        currentTab = tab
    }

    private fun fragmentFor(tab: MainTab): Fragment {
        return tabFragments.getOrPut(tab) {
            supportFragmentManager.findFragmentByTag(fragmentTag(tab))
                ?: when (tab) {
                    MainTab.Home -> HomeFeedFragment()
                    MainTab.Discover -> DiscoverFragment()
                    MainTab.Projects -> ProjectsFragment()
                    MainTab.My -> MyFragment()
                }
        }
    }

    private fun tabFromItemId(itemId: Int): MainTab {
        return when (itemId) {
            R.id.navigation_discover -> MainTab.Discover
            R.id.navigation_projects -> MainTab.Projects
            R.id.navigation_my -> MainTab.My
            else -> MainTab.Home
        }
    }

    private fun fragmentForIfCreated(tab: MainTab): Fragment? {
        return tabFragments[tab] ?: supportFragmentManager.findFragmentByTag(fragmentTag(tab))
    }

    private fun fragmentTag(tab: MainTab): String = "$FRAGMENT_TAG_PREFIX${tab.name.lowercase()}"

    companion object {
        private const val FRAGMENT_TAG_PREFIX = "main_tab_"
        private const val EXTRA_INITIAL_TAB = "initial_tab"

        fun createIntent(context: Context, initialTab: MainTab = MainTab.Home): Intent {
            return Intent(context, HomeActivity::class.java)
                .putExtra(EXTRA_INITIAL_TAB, initialTab.name)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }
}
