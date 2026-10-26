package com.gblrod.radianthub.ui.features.search.components

import com.gblrod.radianthub.ui.features.search.model.SearchFilterType
import com.gblrod.radianthub.ui.features.search.model.SearchItem
import com.gblrod.radianthub.ui.features.search.model.SearchType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchResultsFilterTest {
    @Test
    fun shouldReturnAllResultsWhenAllFilterIsSelected() {
        val results = getSearchResults()

        val filteredResults = filterSearchResults(
            results = results,
            selectedFilter = SearchFilterType.ALL
        )

        assertEquals(results.map { it.uuid }, filteredResults.map { it.uuid })
    }

    @Test
    fun shouldReturnOnlyAgentsWhenAgentsFilterIsSelected() {
        val results = getSearchResults()

        val filteredResults = filterSearchResults(
            results = results,
            selectedFilter = SearchFilterType.AGENTS
        )

        assertEquals(listOf("agent-jett"), filteredResults.map { it.uuid })
    }

    @Test
    fun shouldReturnOnlyMapsWhenMapsFilterIsSelected() {
        val results = getSearchResults()

        val filteredResults = filterSearchResults(
            results = results,
            selectedFilter = SearchFilterType.MAPS
        )

        assertEquals(listOf("map-ascent"), filteredResults.map { it.uuid })
    }

    @Test
    fun shouldReturnOnlyCardsWhenCardsFilterIsSelected() {
        val results = getSearchResults()

        val filteredResults = filterSearchResults(
            results = results,
            selectedFilter = SearchFilterType.CARDS
        )

        assertEquals(listOf("card-jett"), filteredResults.map { it.uuid })
    }

    @Test
    fun shouldReturnOnlyTiersWhenTiersFilterIsSelected() {
        val results = getSearchResults()

        val filteredResults = filterSearchResults(
            results = results,
            selectedFilter = SearchFilterType.TIERS
        )

        assertEquals(listOf("tier-radiant"), filteredResults.map { it.uuid })
    }

    @Test
    fun shouldReturnEmptyListWhenNoResultsMatchSelectedFilter() {
        val results = listOf(
            SearchItem(
                uuid = "agent-jett",
                title = "Jett",
                imageUrl = "",
                type = SearchType.AGENT
            )
        )

        val filteredResults = filterSearchResults(
            results = results,
            selectedFilter = SearchFilterType.MAPS
        )

        assertTrue(filteredResults.isEmpty())
    }
}

private fun getSearchResults(): List<SearchItem> {
    return listOf(
        SearchItem(
            uuid = "agent-jett",
            title = "Jett",
            imageUrl = "",
            type = SearchType.AGENT
        ),
        SearchItem(
            uuid = "map-ascent",
            title = "Ascent",
            imageUrl = "",
            type = SearchType.MAP
        ),
        SearchItem(
            uuid = "card-jett",
            title = "Jett Card",
            imageUrl = "",
            type = SearchType.CARD
        ),
        SearchItem(
            uuid = "tier-radiant",
            title = "Radiant",
            imageUrl = "",
            type = SearchType.TIER
        )
    )
}