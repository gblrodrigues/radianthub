package com.gblrod.radianthub.ui.features.search.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gblrod.radianthub.navigation.NavigationKeys.SELECTED_AGENT_UUID
import com.gblrod.radianthub.navigation.NavigationKeys.SELECTED_MAP_UUID
import com.gblrod.radianthub.navigation.Routes
import com.gblrod.radianthub.navigation.extensions.navigateFromSearch
import com.gblrod.radianthub.navigation.extensions.navigateToTopLevelFromSearch
import com.gblrod.radianthub.ui.features.search.components.EmptySearchResult
import com.gblrod.radianthub.ui.features.search.components.SearchField
import com.gblrod.radianthub.ui.features.search.components.SearchResultItem
import com.gblrod.radianthub.ui.features.search.components.filterSearchResults
import com.gblrod.radianthub.ui.features.search.model.SearchFilterType
import com.gblrod.radianthub.ui.features.search.model.SearchType
import com.gblrod.radianthub.ui.features.search.state.SearchUiState
import com.gblrod.radianthub.ui.features.search.viewmodel.SearchViewModel
import com.gblrod.radianthub.ui.shared.components.ErrorMessage
import com.gblrod.radianthub.ui.shared.components.LoadingScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = koinViewModel(),
    navHostController: NavHostController
) {
    val uiState by searchViewModel.searchState.collectAsState()

    val focus = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var filterExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedFilter by rememberSaveable { mutableStateOf(SearchFilterType.ALL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focus.clearFocus(force = true)
                        keyboardController?.hide()
                    }
                )
            }
    ) {
        SearchField(
            query = when (val state = uiState) {
                is SearchUiState.Loading -> state.query
                is SearchUiState.Error -> state.query
                is SearchUiState.Success -> state.query
            },
            onQueryChange = { searchViewModel.updateQuery(it) },
            onBackClick = { navHostController.popBackStack() },
            expanded = filterExpanded,
            onSearchFilterMenu = { filterExpanded = true },
            onDismissFilterMenu = { filterExpanded = false },
            selectedFilter = selectedFilter,
            onFilterSelected = { filter ->
                selectedFilter = filter
                filterExpanded = false
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            when (val state = uiState) {
                is SearchUiState.Loading -> {
                    LoadingScreen()
                }

                is SearchUiState.Error -> {
                    val message = state.code?.let {
                        stringResource(
                            id = state.messageResId,
                            it
                        )
                    } ?: stringResource(id = state.messageResId)

                    ErrorMessage(
                        message = message,
                        onRetry = { searchViewModel.retry() }
                    )
                }

                is SearchUiState.Success -> {
                    val filteredResults = filterSearchResults(
                        results = state.results,
                        selectedFilter = selectedFilter
                    )

                    if (filteredResults.isEmpty()) {
                        EmptySearchResult(query = state.query)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 4.dp,
                                bottom = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = filteredResults,
                                key = { it.uuid + it.type.name }
                            ) { item ->
                                SearchResultItem(
                                    item = item,
                                    onClick = {
                                        when (item.type) {
                                            SearchType.AGENT -> {
                                                navHostController.navigateToTopLevelFromSearch(
                                                    targetRoute = Routes.Agents.ROUTE,
                                                    selectionKey = SELECTED_AGENT_UUID,
                                                    selectionValue = item.uuid
                                                )
                                            }

                                            SearchType.MAP -> {
                                                navHostController.navigateToTopLevelFromSearch(
                                                    targetRoute = Routes.Maps.ROUTE,
                                                    selectionKey = SELECTED_MAP_UUID,
                                                    selectionValue = item.uuid
                                                )
                                            }

                                            SearchType.CARD -> {
                                                navHostController.navigateFromSearch(
                                                    targetRoute = Routes.Cards.createRoute(cardUuid = item.uuid)
                                                )
                                            }

                                            SearchType.TIER -> {
                                                item.tierId?.let { tierId ->
                                                    navHostController.navigateFromSearch(
                                                        targetRoute = Routes.Tiers.createRoute(tierId = tierId)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}