package com.gblrod.radianthub.ui.features.search.viewmodel

import androidx.lifecycle.ViewModelStore
import com.gblrod.radianthub.R
import com.gblrod.radianthub.core.connectivity.RetryManager
import com.gblrod.radianthub.domain.agents.repository.AgentsRepository
import com.gblrod.radianthub.domain.cards.repository.CardsRepository
import com.gblrod.radianthub.domain.maps.repository.MapsRepository
import com.gblrod.radianthub.domain.tiers.repository.TiersRepository
import com.gblrod.radianthub.rules.MainDispatcherRule
import com.gblrod.radianthub.ui.features.search.model.SearchType
import com.gblrod.radianthub.ui.features.search.state.SearchUiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private lateinit var agentsRepository: AgentsRepository
    private lateinit var mapsRepository: MapsRepository
    private lateinit var cardsRepository: CardsRepository
    private lateinit var tiersRepository: TiersRepository
    private lateinit var retryManager: RetryManager
    private lateinit var viewModel: SearchViewModel
    private lateinit var viewModelStore: ViewModelStore

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(
        testDispatcher = StandardTestDispatcher()
    )

    @Before
    fun setUp() {
        agentsRepository = mockk()
        mapsRepository = mockk()
        cardsRepository = mockk()
        tiersRepository = mockk()
        retryManager = mockk()
        viewModelStore = ViewModelStore()

        every { retryManager.retryAll } returns MutableSharedFlow()
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
    }

    @Test
    fun shouldKeepLoadingStateWhileRepositoriesAreLoading() = runTest {
        val agentsLoadGate = CompletableDeferred<Unit>()
        stubEmptyRepositories()

        coEvery { agentsRepository.getAgents() } coAnswers {
            agentsLoadGate.await()
            emptyList()
        }

        viewModel = createViewModel()

        runCurrent()

        assertTrue(viewModel.searchState.value is SearchUiState.Loading)
        agentsLoadGate.complete(Unit)
        advanceUntilIdle()
        assertTrue(viewModel.searchState.value is SearchUiState.Success)
    }

    @Test
    fun shouldLoadResultsFromAllRepositories() = runTest {
        stubSuccessfulRepositories()

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.searchState.value
        assertTrue(state is SearchUiState.Success)
        state as SearchUiState.Success

        assertEquals(4, state.results.size)
        assertEquals(
            listOf(SearchType.AGENT, SearchType.MAP, SearchType.CARD, SearchType.TIER),
            state.results.map { it.type })
    }

    @Test
    fun shouldFilterResultsByQueryIgnoringCase() = runTest {
        stubSuccessfulRepositories()

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateQuery("jEtT")

        val state = viewModel.searchState.value
        assertTrue(state is SearchUiState.Success)
        state as SearchUiState.Success

        assertEquals("jEtT", state.query)
        assertEquals(listOf("Jett", "Jett Card"), state.results.map { it.title })
    }

    @Test
    fun shouldReturnEmptyResultsWhenNoItemMatchesQuery() = runTest {
        stubSuccessfulRepositories()

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateQuery("nonexistent")

        val state = viewModel.searchState.value
        assertTrue(state is SearchUiState.Success)
        state as SearchUiState.Success

        assertEquals("nonexistent", state.query)
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun shouldPreserveLatestQueryEnteredWhileLoading() = runTest {
        val agentsLoadGate = CompletableDeferred<Unit>()

        coEvery { agentsRepository.getAgents() } coAnswers {
            agentsLoadGate.await()

            listOf(
                mockk {
                    every { uuid } returns "agent-jett"
                    every { name } returns "Jett"
                    every { icon } returns ""
                }
            )
        }

        coEvery { mapsRepository.getMaps() } returns emptyList()
        coEvery { cardsRepository.getCards() } returns emptyList()
        coEvery { tiersRepository.getTiers() } returns emptyList()

        viewModel = createViewModel()

        runCurrent()
        viewModel.updateQuery("Jett")

        agentsLoadGate.complete(Unit)
        advanceUntilIdle()

        val state = viewModel.searchState.value
        assertTrue(state is SearchUiState.Success)
        state as SearchUiState.Success

        assertEquals("Jett", state.query)
        assertEquals(listOf("Jett"), state.results.map { it.title })
    }

    @Test
    fun shouldExposeErrorStateWhenRepositoryFails() = runTest {
        coEvery {
            agentsRepository.getAgents()
        } throws IOException("Network error")

        coEvery { mapsRepository.getMaps() } returns emptyList()
        coEvery { cardsRepository.getCards() } returns emptyList()
        coEvery { tiersRepository.getTiers() } returns emptyList()

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.searchState.value
        assertTrue(state is SearchUiState.Error)
        state as SearchUiState.Error
        assertEquals(R.string.ui_state_io_exception, state.messageResId)
    }

    private fun createViewModel(): SearchViewModel {
        return SearchViewModel(
            agentsRepository = agentsRepository,
            mapsRepository = mapsRepository,
            cardsRepository = cardsRepository,
            tiersRepository = tiersRepository,
            retryManager = retryManager
        ).also {
            viewModelStore.put("search", it)
        }
    }

    private fun stubEmptyRepositories() {
        coEvery { agentsRepository.getAgents() } returns emptyList()
        coEvery { mapsRepository.getMaps() } returns emptyList()
        coEvery { cardsRepository.getCards() } returns emptyList()
        coEvery { tiersRepository.getTiers() } returns emptyList()
    }

    private fun stubSuccessfulRepositories() {
        coEvery {
            agentsRepository.getAgents()
        } returns listOf(
            mockk {
                every { uuid } returns "agent-jett"
                every { name } returns "Jett"
                every { icon } returns ""
            }
        )

        coEvery {
            mapsRepository.getMaps()
        } returns listOf(
            mockk {
                every { uuid } returns "map-ascent"
                every { name } returns "Ascent"
                every { icon } returns ""
            }
        )

        coEvery {
            cardsRepository.getCards()
        } returns listOf(
            mockk {
                every { uuid } returns "card-jett"
                every { name } returns "Jett Card"
                every { smallArt } returns ""
            }
        )

        coEvery {
            tiersRepository.getTiers()
        } returns listOf(
            mockk {
                every { tierName } returns "Radiant"
                every { icon } returns ""
                every { tier } returns 1
            }
        )
    }
}