package com.passgo.app.feature.search

import app.cash.turbine.test
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.SearchHistory
import com.passgo.app.core.model.VaultItem
import com.passgo.app.core.model.VaultItemCategory
import com.passgo.app.data.repository.SearchHistoryRepository
import com.passgo.app.data.repository.VaultItemRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var vaultItemRepository: VaultItemRepository
    private lateinit var searchHistoryRepository: SearchHistoryRepository
    private lateinit var viewModel: SearchViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        vaultItemRepository = mockk()
        searchHistoryRepository = mockk()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle with empty query`() = runTest(testDispatcher) {
        every { vaultItemRepository.searchItemsFts(any(), any()) } returns flowOf(emptyList())
        every { searchHistoryRepository.getRecentSearches(any()) } returns flowOf(emptyList())

        viewModel = SearchViewModel(vaultItemRepository, searchHistoryRepository)

        assertEquals("", viewModel.query.value)
        assertTrue(viewModel.searchState.value is SearchState.Idle)
        assertTrue(viewModel.recentSearches.value.isEmpty())
    }

    @Test
    fun `query update is reflected immediately`() = runTest(testDispatcher) {
        every { vaultItemRepository.searchItemsFts(any(), any()) } returns flowOf(emptyList())
        every { searchHistoryRepository.getRecentSearches(any()) } returns flowOf(emptyList())

        viewModel = SearchViewModel(vaultItemRepository, searchHistoryRepository)

        viewModel.onQueryChanged("github")

        assertEquals("github", viewModel.query.value)
    }

    @Test
    fun `empty query stays in Idle state`() = runTest(testDispatcher) {
        every { vaultItemRepository.searchItemsFts(any(), any()) } returns flowOf(emptyList())
        every { searchHistoryRepository.getRecentSearches(any()) } returns flowOf(emptyList())

        viewModel = SearchViewModel(vaultItemRepository, searchHistoryRepository)

        viewModel.onQueryChanged("")
        assertTrue(viewModel.searchState.value is SearchState.Idle)
    }

    @Test
    fun `search returns results after debounce`() = runTest(testDispatcher) {
        val items = listOf(
            VaultItem(id = "1", vaultId = "default", name = "GitHub", category = VaultItemCategory.GOOGLE_ACCOUNT)
        )

        every { vaultItemRepository.searchItemsFts(any(), "git*") } returns flowOf(items)
        every { searchHistoryRepository.getRecentSearches(any()) } returns flowOf(emptyList())

        viewModel = SearchViewModel(vaultItemRepository, searchHistoryRepository)

        val job = launch { viewModel.searchState.collect { } }
        viewModel.onQueryChanged("git")
        advanceUntilIdle()
        val state = viewModel.searchState.value
        assertTrue(state is SearchState.Results, "Expected Results but got $state")
        assertEquals(1, (state as SearchState.Results).items.size)
        assertEquals("GitHub", state.items.first().name)
        job.cancel()
    }

    @Test
    fun `search returns empty state when no results`() = runTest(testDispatcher) {
        every { vaultItemRepository.searchItemsFts(any(), "xyz*") } returns flowOf(emptyList())
        every { searchHistoryRepository.getRecentSearches(any()) } returns flowOf(emptyList())

        viewModel = SearchViewModel(vaultItemRepository, searchHistoryRepository)

        val job = launch { viewModel.searchState.collect { } }
        viewModel.onQueryChanged("xyz")
        advanceUntilIdle()
        val state = viewModel.searchState.value
        assertTrue(state is SearchState.Empty, "Expected Empty but got $state")
        job.cancel()
    }

    @Test
    fun `category filter uses searchByTypeFts`() = runTest(testDispatcher) {
        val items = listOf(
            VaultItem(id = "2", vaultId = "default", name = "Gmail", category = VaultItemCategory.EMAIL)
        )

        every { vaultItemRepository.searchByTypeFts(any(), eq(VaultItemCategory.EMAIL), "gmail*") } returns flowOf(items)
        every { searchHistoryRepository.getRecentSearches(any()) } returns flowOf(emptyList())

        viewModel = SearchViewModel(vaultItemRepository, searchHistoryRepository)

        val job = launch { viewModel.searchState.collect { } }
        viewModel.onCategorySelected(VaultItemCategory.EMAIL)
        viewModel.onQueryChanged("gmail")
        advanceUntilIdle()
        val state = viewModel.searchState.value
        assertTrue(state is SearchState.Results, "Expected Results but got $state")
        assertEquals(1, (state as SearchState.Results).items.size)
        assertEquals("Gmail", state.items.first().name)
        job.cancel()
    }

    @Test
    fun `recent searches are exposed`() = runTest(testDispatcher) {
        val searches = listOf(
            SearchHistory(id = "h1", vaultId = "default", query = "github", createdAt = 1000L),
            SearchHistory(id = "h2", vaultId = "default", query = "aws", createdAt = 2000L)
        )

        every { vaultItemRepository.searchItemsFts(any(), any()) } returns flowOf(emptyList())
        every { searchHistoryRepository.getRecentSearches(any()) } returns flowOf(searches)

        viewModel = SearchViewModel(vaultItemRepository, searchHistoryRepository)

        viewModel.recentSearches.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertEquals("github", items.first().query)
        }
    }

    @Test
    fun `tapping recent search sets query and records search`() = runTest(testDispatcher) {
        every { vaultItemRepository.searchItemsFts(any(), any()) } returns flowOf(emptyList())
        every { searchHistoryRepository.getRecentSearches(any()) } returns flowOf(emptyList())
        coEvery { searchHistoryRepository.recordSearch(any(), any()) } returns AppResult.Success(Unit)

        viewModel = SearchViewModel(vaultItemRepository, searchHistoryRepository)

        viewModel.onRecentSearchTapped("github")

        assertEquals("github", viewModel.query.value)
        coVerify { searchHistoryRepository.recordSearch("default", "github") }
    }

    @Test
    fun `deleting recent search calls repository`() = runTest(testDispatcher) {
        every { vaultItemRepository.searchItemsFts(any(), any()) } returns flowOf(emptyList())
        every { searchHistoryRepository.getRecentSearches(any()) } returns flowOf(emptyList())
        coEvery { searchHistoryRepository.deleteSearch(any()) } returns AppResult.Success(Unit)

        viewModel = SearchViewModel(vaultItemRepository, searchHistoryRepository)

        viewModel.onDeleteRecentSearch("h1")

        coVerify { searchHistoryRepository.deleteSearch("h1") }
    }

    @Test
    fun `clearing recent searches calls repository`() = runTest(testDispatcher) {
        every { vaultItemRepository.searchItemsFts(any(), any()) } returns flowOf(emptyList())
        every { searchHistoryRepository.getRecentSearches(any()) } returns flowOf(emptyList())
        coEvery { searchHistoryRepository.deleteAllForVault(any()) } returns AppResult.Success(Unit)

        viewModel = SearchViewModel(vaultItemRepository, searchHistoryRepository)

        viewModel.onClearRecentSearches()

        coVerify { searchHistoryRepository.deleteAllForVault("default") }
    }

    @Test
    fun `search execution records search`() = runTest(testDispatcher) {
        every { vaultItemRepository.searchItemsFts(any(), any()) } returns flowOf(emptyList())
        every { searchHistoryRepository.getRecentSearches(any()) } returns flowOf(emptyList())
        coEvery { searchHistoryRepository.recordSearch(any(), any()) } returns AppResult.Success(Unit)

        viewModel = SearchViewModel(vaultItemRepository, searchHistoryRepository)

        viewModel.onSearchExecuted("github")

        coVerify { searchHistoryRepository.recordSearch("default", "github") }
    }

    @Test
    fun `repository exception emits Error state`() = runTest(testDispatcher) {
        every { vaultItemRepository.searchItemsFts(any(), any()) } throws
            RuntimeException("Network error")
        every { searchHistoryRepository.getRecentSearches(any()) } returns flowOf(emptyList())

        viewModel = SearchViewModel(vaultItemRepository, searchHistoryRepository)

        val job = launch { viewModel.searchState.collect { } }
        viewModel.onQueryChanged("test")
        advanceUntilIdle()
        val state = viewModel.searchState.value
        assertTrue(state is SearchState.Error, "Expected Error but got $state")
        job.cancel()
    }

    @Test
    fun `uiState reflects all properties`() = runTest(testDispatcher) {
        val searches = listOf(
            SearchHistory(id = "h1", vaultId = "default", query = "aws", createdAt = 1000L)
        )
        val items = listOf(
            VaultItem(id = "1", vaultId = "default", name = "AWS", category = VaultItemCategory.GOOGLE_ACCOUNT)
        )

        every { vaultItemRepository.searchItemsFts(any(), "aws*") } returns flowOf(items)
        every { searchHistoryRepository.getRecentSearches(any()) } returns flowOf(searches)

        viewModel = SearchViewModel(vaultItemRepository, searchHistoryRepository)

        val job = launch { viewModel.uiState.collect { } }
        viewModel.onQueryChanged("aws")
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("aws", state.query)
        assertTrue(state.searchState is SearchState.Results, "Expected Results but got ${state.searchState}")
        assertEquals(1, (state.searchState as SearchState.Results).items.size)
        job.cancel()
    }
}
