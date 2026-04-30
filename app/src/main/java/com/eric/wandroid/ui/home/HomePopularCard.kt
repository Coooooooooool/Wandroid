package com.eric.wandroid.ui.home

import com.eric.wandroid.domain.model.Article
import com.eric.wandroid.domain.model.PopularColumn
import com.eric.wandroid.domain.model.PopularRoute

sealed interface HomePopularCard {
    data class RouteFocus(val routes: List<PopularRoute>) : HomePopularCard

    data class WendaFocus(val wenda: List<Article>) : HomePopularCard

    data class ColumnFocus(val columns: List<PopularColumn>) : HomePopularCard
}
