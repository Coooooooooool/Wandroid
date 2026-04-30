package com.eric.wandroid.ui.shell

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.common.auth.LOGIN_EXPIRED_CODE
import com.eric.wandroid.common.auth.navigateToLogin
import com.eric.wandroid.R
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.AccountRepository
import com.eric.wandroid.data.repository.AuthRepository
import com.eric.wandroid.data.repository.ExtraContentMode
import com.eric.wandroid.domain.model.CoinOverview
import com.eric.wandroid.domain.model.UserProfile
import com.eric.wandroid.domain.model.UserSession
import com.eric.wandroid.ui.auth.AuthActivity
import com.eric.wandroid.ui.coin.PointsActivity
import com.eric.wandroid.ui.extra.ExtraContentActivity
import com.eric.wandroid.ui.history.ReadingHistoryActivity
import com.eric.wandroid.ui.message.MessagesActivity
import com.eric.wandroid.ui.profile.CollectProfileActivity
import com.eric.wandroid.ui.settings.SettingsActivity
import com.eric.wandroid.ui.todo.TodoActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MyFragment : Fragment() {
    private val authRepository: AuthRepository by lazy(LazyThreadSafetyMode.NONE) {
        AuthRepository(NetworkModule.apiService, NetworkModule.requireSessionStore())
    }
    private val accountRepository: AccountRepository by lazy(LazyThreadSafetyMode.NONE) {
        AccountRepository(NetworkModule.apiService, NetworkModule.requireSessionStore())
    }

    private var headerCard: MaterialCardView? = null
    private var statusTitle: TextView? = null
    private var statusSubtitle: TextView? = null
    private var metaLine: TextView? = null
    private var statsLine: TextView? = null
    private var favoritesButton: Button? = null
    private var pointsButton: Button? = null
    private var todoButton: Button? = null
    private var messageButton: Button? = null
    private var privateShareButton: Button? = null
    private var readingHistoryButton: Button? = null
    private var settingsButton: Button? = null
    private var logoutButton: Button? = null
    private var currentSession: UserSession? = null
    private var isLoggingOut: Boolean = false
    private var isLoadingProfile: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_my, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        headerCard?.setOnClickListener {
            if (currentSession == null) {
                startActivity(Intent(requireContext(), AuthActivity::class.java))
            }
        }
        favoritesButton?.setOnClickListener {
            startActivity(Intent(requireContext(), CollectProfileActivity::class.java))
        }
        pointsButton?.setOnClickListener {
            startActivity(Intent(requireContext(), PointsActivity::class.java))
        }
        todoButton?.setOnClickListener {
            startActivity(TodoActivity.createIntent(requireContext()))
        }
        messageButton?.setOnClickListener {
            startActivity(MessagesActivity.createIntent(requireContext()))
        }
        privateShareButton?.setOnClickListener {
            startActivity(ExtraContentActivity.createIntent(requireContext(), ExtraContentMode.PrivateShare))
        }
        readingHistoryButton?.setOnClickListener {
            startActivity(ReadingHistoryActivity.createIntent(requireContext()))
        }
        settingsButton?.setOnClickListener {
            startActivity(SettingsActivity.createIntent(requireContext()))
        }
        logoutButton?.setOnClickListener { logout() }
        observeSession()
    }

    private fun bindViews(view: View) {
        headerCard = view.findViewById(R.id.myHeaderCard)
        statusTitle = view.findViewById(R.id.myStatusTitle)
        statusSubtitle = view.findViewById(R.id.myStatusSubtitle)
        metaLine = view.findViewById(R.id.myMetaLine)
        statsLine = view.findViewById(R.id.myStatsLine)
        favoritesButton = view.findViewById(R.id.myFavoritesButton)
        pointsButton = view.findViewById(R.id.myPointsButton)
        todoButton = view.findViewById(R.id.myTodoButton)
        messageButton = view.findViewById(R.id.myMessagesButton)
        privateShareButton = view.findViewById(R.id.myPrivateShareButton)
        readingHistoryButton = view.findViewById(R.id.myReadingHistoryButton)
        settingsButton = view.findViewById(R.id.mySettingsButton)
        logoutButton = view.findViewById(R.id.myLogoutButton)
    }

    private fun observeSession() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                NetworkModule.requireSessionStore().sessionState.collect(::renderSession)
            }
        }
    }

    private fun renderSession(session: UserSession?) {
        currentSession = session
        val loggedIn = session != null
        statusTitle?.text = if (loggedIn) {
            getString(R.string.my_logged_in_title, session?.displayName.orEmpty())
        } else {
            getString(R.string.my_guest_title)
        }
        statusSubtitle?.text = if (loggedIn) {
            getString(R.string.my_logged_in_summary, session?.username.orEmpty())
        } else {
            getString(R.string.my_guest_summary)
        }
        favoritesButton?.isEnabled = loggedIn
        pointsButton?.isEnabled = loggedIn
        todoButton?.isEnabled = loggedIn
        messageButton?.isEnabled = loggedIn
        privateShareButton?.isEnabled = loggedIn
        readingHistoryButton?.isEnabled = true
        settingsButton?.isEnabled = true
        logoutButton?.visibility = if (loggedIn) View.VISIBLE else View.GONE
        logoutButton?.isEnabled = loggedIn && !isLoggingOut

        if (loggedIn) {
            metaLine?.text = getString(
                R.string.my_profile_meta,
                session?.username.orEmpty().ifBlank { "-" },
                session?.email.orEmpty().ifBlank { "-" }
            )
            statsLine?.text = getString(R.string.my_stats_loading)
            loadProfileSummary()
        } else {
            metaLine?.text = getString(R.string.my_guest_meta)
            statsLine?.text = getString(R.string.my_guest_stats)
        }
    }

    private fun loadProfileSummary() {
        if (isLoadingProfile || currentSession == null) return
        isLoadingProfile = true
        viewLifecycleOwner.lifecycleScope.launch {
            val profileResult = accountRepository.loadUserProfile()
            val coinResult = accountRepository.loadCoinOverview()
            val authError = listOf(profileResult, coinResult)
                .filterIsInstance<AppResult.Error>()
                .firstOrNull { it.code == LOGIN_EXPIRED_CODE }

            val profile = (profileResult as? AppResult.Success)?.data
            val coinOverview = (coinResult as? AppResult.Success)?.data

            renderProfileMeta(profile)
            renderProfileStats(profile, coinOverview)
            isLoadingProfile = false

            authError?.let { error ->
                activity?.navigateToLogin(error.message)
            }
        }
    }

    private fun renderProfileMeta(profile: UserProfile?) {
        val session = currentSession
        val username = profile?.username
            .orEmpty()
            .ifBlank { session?.username.orEmpty() }
            .ifBlank { "-" }
        val email = profile?.email
            .orEmpty()
            .ifBlank { session?.email.orEmpty() }
            .ifBlank { "-" }

        metaLine?.text = getString(R.string.my_profile_meta, username, email)
    }

    private fun renderProfileStats(profile: UserProfile?, coinOverview: CoinOverview?) {
        val coinCount = profile?.coinCount ?: coinOverview?.coinCount
        val level = profile?.level
        val rank = profile?.rank
            .orEmpty()
            .ifBlank { coinOverview?.rank?.toString().orEmpty() }

        statsLine?.text = if (coinCount != null && level != null) {
            getString(
                R.string.my_profile_stats,
                coinCount,
                level,
                rank.ifBlank { "-" }
            )
        } else {
            getString(R.string.my_profile_stats_fallback)
        }
    }

    private fun logout() {
        if (currentSession == null || isLoggingOut) return
        isLoggingOut = true
        logoutButton?.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = authRepository.logout()) {
                is AppResult.Success -> {
                    view?.let { root ->
                        Snackbar.make(root, "Signed out.", Snackbar.LENGTH_SHORT).show()
                    }
                }
                is AppResult.Error -> {
                    view?.let { root ->
                        Snackbar.make(root, result.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            isLoggingOut = false
            val loggedIn = currentSession != null
            logoutButton?.isEnabled = loggedIn
        }
    }

    override fun onDestroyView() {
        headerCard = null
        statusTitle = null
        statusSubtitle = null
        metaLine = null
        statsLine = null
        favoritesButton = null
        pointsButton = null
        todoButton = null
        messageButton = null
        privateShareButton = null
        readingHistoryButton = null
        settingsButton = null
        logoutButton = null
        super.onDestroyView()
    }
}
