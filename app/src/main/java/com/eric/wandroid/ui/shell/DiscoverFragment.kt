package com.eric.wandroid.ui.shell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.eric.wandroid.R
import com.eric.wandroid.data.repository.ExtraContentMode
import com.eric.wandroid.ui.extra.ExtraContentActivity
import com.eric.wandroid.ui.project.ProjectSearchActivity
import com.eric.wandroid.ui.site.CommonWebsitesActivity
import com.eric.wandroid.ui.system.SystemContentMode
import com.eric.wandroid.ui.system.SystemNavigationActivity
import com.eric.wandroid.ui.system.SystemRootListActivity
import com.google.android.material.card.MaterialCardView

class DiscoverFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindEntry(
            root = view,
            cardId = R.id.entrySystem,
            titleId = R.id.entrySystemTitle,
            summaryId = R.id.entrySystemSummary,
            titleRes = R.string.discover_system_only_title,
            summaryRes = R.string.discover_system_only_summary
        ) {
            startActivity(
                SystemRootListActivity.createIntent(requireContext())
            )
        }
        bindEntry(
            root = view,
            cardId = R.id.entryNavigation,
            titleId = R.id.entryNavigationTitle,
            summaryId = R.id.entryNavigationSummary,
            titleRes = R.string.discover_navigation_title,
            summaryRes = R.string.discover_navigation_summary
        ) {
            startActivity(
                SystemNavigationActivity.createIntent(requireContext(), SystemContentMode.Navigation)
            )
        }
        bindEntry(
            root = view,
            cardId = R.id.entryCommunity,
            titleId = R.id.entryCommunityTitle,
            summaryId = R.id.entryCommunitySummary,
            titleRes = R.string.discover_wenda_title,
            summaryRes = R.string.discover_wenda_summary
        ) {
            startActivity(ExtraContentActivity.createIntent(requireContext(), ExtraContentMode.Wenda))
        }
        bindEntry(
            root = view,
            cardId = R.id.entrySquare,
            titleId = R.id.entrySquareTitle,
            summaryId = R.id.entrySquareSummary,
            titleRes = R.string.discover_square_title,
            summaryRes = R.string.discover_square_summary
        ) {
            startActivity(ExtraContentActivity.createIntent(requireContext(), ExtraContentMode.Square))
        }
        bindEntry(
            root = view,
            cardId = R.id.entryWechat,
            titleId = R.id.entryWechatTitle,
            summaryId = R.id.entryWechatSummary,
            titleRes = R.string.discover_wechat_title,
            summaryRes = R.string.discover_wechat_summary
        ) {
            startActivity(ExtraContentActivity.createIntent(requireContext(), ExtraContentMode.Wechat))
        }
        bindEntry(
            root = view,
            cardId = R.id.entryProjectCategories,
            titleId = R.id.entryProjectCategoriesTitle,
            summaryId = R.id.entryProjectCategoriesSummary,
            titleRes = R.string.projects_categories_title,
            summaryRes = R.string.projects_categories_summary
        ) {
            startActivity(android.content.Intent(requireContext(), ProjectSearchActivity::class.java))
        }
        bindEntry(
            root = view,
            cardId = R.id.entryLatestProject,
            titleId = R.id.entryLatestProjectTitle,
            summaryId = R.id.entryLatestProjectSummary,
            titleRes = R.string.projects_latest_title,
            summaryRes = R.string.projects_latest_summary
        ) {
            startActivity(ExtraContentActivity.createIntent(requireContext(), ExtraContentMode.LatestProject))
        }
        bindEntry(
            root = view,
            cardId = R.id.entryWebsites,
            titleId = R.id.entryWebsitesTitle,
            summaryId = R.id.entryWebsitesSummary,
            titleRes = R.string.section_websites,
            summaryRes = R.string.discover_websites_summary
        ) {
            startActivity(CommonWebsitesActivity.createIntent(requireContext()))
        }
    }

    private fun bindEntry(
        root: View,
        cardId: Int,
        titleId: Int,
        summaryId: Int,
        titleRes: Int,
        summaryRes: Int,
        onClick: () -> Unit
    ) {
        root.findViewById<MaterialCardView>(cardId).setOnClickListener { onClick() }
        root.findViewById<TextView>(titleId).setText(titleRes)
        root.findViewById<TextView>(summaryId).setText(summaryRes)
    }
}
