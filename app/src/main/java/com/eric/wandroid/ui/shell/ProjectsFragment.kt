package com.eric.wandroid.ui.shell

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.eric.wandroid.R
import com.eric.wandroid.ui.moyu.MoyuVideoActivity
import com.eric.wandroid.ui.moyu.internet.InternetNewsActivity
import com.eric.wandroid.ui.moyu.news.NewsListActivity
import com.google.android.material.card.MaterialCardView

class ProjectsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_projects, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialCardView>(R.id.entryMoyuVideo).setOnClickListener {
            startActivity(MoyuVideoActivity.createIntent(requireContext()))
        }
        view.findViewById<MaterialCardView>(R.id.entryMoyuNews).setOnClickListener {
            startActivity(NewsListActivity.createIntent(requireContext()))
        }
        view.findViewById<MaterialCardView>(R.id.entryInternetNews).setOnClickListener {
            startActivity(InternetNewsActivity.createIntent(requireContext()))
        }
        view.findViewById<TextView>(R.id.entryMoyuVideoTitle).setText(R.string.moyu_video_title)
        view.findViewById<TextView>(R.id.entryMoyuVideoSummary).setText(R.string.moyu_video_summary)
        view.findViewById<TextView>(R.id.entryMoyuNewsTitle).setText(R.string.moyu_news_title)
        view.findViewById<TextView>(R.id.entryMoyuNewsSummary).setText(R.string.moyu_news_summary)
        view.findViewById<TextView>(R.id.entryInternetNewsTitle).setText(R.string.moyu_internet_news_title)
        view.findViewById<TextView>(R.id.entryInternetNewsSummary).setText(R.string.moyu_internet_news_summary)
    }
}
