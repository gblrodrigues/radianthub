package com.gblrod.radianthub.ui.features.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gblrod.radianthub.R
import com.gblrod.radianthub.core.connectivity.RetryManager
import com.gblrod.radianthub.domain.agents.repository.AgentsRepository
import com.gblrod.radianthub.domain.cards.repository.CardsRepository
import com.gblrod.radianthub.domain.maps.repository.MapsRepository
import com.gblrod.radianthub.domain.tiers.repository.TiersRepository
import com.gblrod.radianthub.ui.features.search.model.SearchItem
import com.gblrod.radianthub.ui.features.search.model.SearchType
import com.gblrod.radianthub.ui.features.search.state.SearchUiState
import com.gblrod.radianthub.ui.shared.utils.safeApiCall
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val agentsRepository: AgentsRepository,
    private val mapsRepository: MapsRepository,
    private val cardsRepository: CardsRepository,
    private val tiersRepository: TiersRepository,
    private val retryManager: RetryManager
) : ViewModel() {

    private val _searchState =
        MutableStateFlow<SearchUiState>(SearchUiState.Loading())
    val searchState: StateFlow<SearchUiState> = _searchState

    private var allItems: List<SearchItem> = emptyList()

    init {
        observeRetry()
        loadItems()
    }

    private fun observeRetry() {
        viewModelScope.launch {
            retryManager.retryAll.collect {
                if (_searchState.value is SearchUiState.Error) {
                    loadItems()
                }
            }
        }
    }

    fun updateQuery(query: String) {
        when (val state = _searchState.value) {
            is SearchUiState.Loading -> _searchState.value = state.copy(query = query)
            is SearchUiState.Error -> _searchState.value = state.copy(query = query)

            is SearchUiState.Success -> {
                val filteredItems = allItems.filter { item ->
                    item.title.contains(
                        other = query,
                        ignoreCase = true
                    )
                }

                _searchState.value =
                    state.copy(
                        query = query,
                        results = filteredItems
                    )
            }
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            val initialQuery = getCurrentQuery()

            _searchState.value = SearchUiState.Loading(query = initialQuery)

            safeApiCall(
                onHttpError = { code ->
                    _searchState.value =
                        SearchUiState.Error(
                            query = getCurrentQuery(),
                            messageResId = R.string.ui_state_http_exception,
                            code = code
                        )
                },
                onIoError = {
                    _searchState.value =
                        SearchUiState.Error(
                            query = getCurrentQuery(),
                            messageResId = R.string.ui_state_io_exception
                        )
                },
                onGenericError = {
                    _searchState.value =
                        SearchUiState.Error(
                            query = getCurrentQuery(),
                            messageResId = R.string.ui_state_generic_error
                        )
                }
            ) {
                coroutineScope {
                    val agentsDeferred = async {
                        agentsRepository.getAgents()
                    }

                    val cardsDeferred = async {
                        cardsRepository.getCards()
                    }

                    val mapsDeferred = async {
                        mapsRepository.getMaps()
                    }

                    val tiersDeferred = async {
                        tiersRepository.getTiers()
                    }

                    val agents = agentsDeferred.await()
                    val cards = cardsDeferred.await()
                    val maps = mapsDeferred.await()
                    val tiers = tiersDeferred.await()

                    val agentsItems = agents.map { agent ->
                        SearchItem(
                            uuid = agent.uuid,
                            title = agent.name,
                            imageUrl = agent.icon.orEmpty(),
                            type = SearchType.AGENT
                        )
                    }

                    val cardsItems = cards.map { card ->
                        SearchItem(
                            uuid = card.uuid,
                            title = card.name,
                            imageUrl = card.smallArt.orEmpty(),
                            type = SearchType.CARD
                        )
                    }

                    val mapsItems = maps.map { map ->
                        SearchItem(
                            uuid = map.uuid,
                            title = map.name,
                            imageUrl = map.icon.orEmpty(),
                            type = SearchType.MAP
                        )
                    }

                    val tiersItems = tiers.map { tier ->
                        SearchItem(
                            uuid = tier.tierName,
                            title = tier.tierName,
                            imageUrl = tier.icon.orEmpty(),
                            type = SearchType.TIER,
                            tierId = tier.tier
                        )
                    }

                    allItems = agentsItems + mapsItems + cardsItems + tiersItems

                    val latestQuery = getCurrentQuery()

                    val filteredItems = allItems.filter { item ->
                        item.title.contains(
                            other = latestQuery,
                            ignoreCase = true
                        )
                    }

                    _searchState.value = SearchUiState.Success(
                        query = latestQuery,
                        results = filteredItems
                    )
                }
            }
        }
    }

    fun retry() {
        retryManager.retry()
    }

    private fun getCurrentQuery(): String =
        when (val state = _searchState.value) {
            is SearchUiState.Loading -> state.query
            is SearchUiState.Error -> state.query
            is SearchUiState.Success -> state.query
        }
}