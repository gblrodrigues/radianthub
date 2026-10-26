package com.gblrod.radianthub.ui.features.search.components

import com.gblrod.radianthub.ui.features.search.model.SearchFilterType
import com.gblrod.radianthub.ui.features.search.model.SearchItem
import com.gblrod.radianthub.ui.features.search.model.SearchType

fun filterSearchResults(
    results: List<SearchItem>,
    selectedFilter: SearchFilterType
): List<SearchItem> {
    return when (selectedFilter) {
        SearchFilterType.ALL -> results
        SearchFilterType.AGENTS -> results.filter { it.type == SearchType.AGENT }
        SearchFilterType.MAPS -> results.filter { it.type == SearchType.MAP }
        SearchFilterType.CARDS -> results.filter { it.type == SearchType.CARD }
        SearchFilterType.TIERS -> results.filter { it.type == SearchType.TIER }
    }
}