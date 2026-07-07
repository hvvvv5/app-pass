package com.passgo.app.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.SearchHistory
import com.passgo.app.core.model.VaultItem
import com.passgo.app.core.model.VaultItemCategory
import com.passgo.app.data.repository.SearchHistoryRepository
import com.passgo.app.data.repository.VaultItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchState {
    data object Idle : SearchState()
    data class Loading(val query: String) : SearchState()
    data class Results(val items: List<VaultItem>, val query: String) : SearchState()
    data object Empty : SearchState()
    data class Error(val message: String) : SearchState()
}

data class SearchUiState(
    val query: String = "",
    val searchState: SearchState = SearchState.Idle,
    val recentSearches: List<SearchHistory> = emptyList(),
    val selectedCategory: VaultItemCategory? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val vaultItemRepository: VaultItemRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    private val vaultId = "default"

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedCategory = MutableStateFlow<VaultItemCategory?>(null)
    val selectedCategory: StateFlow<VaultItemCategory?> = _selectedCategory.asStateFlow()

    val searchState: StateFlow<SearchState> = _query
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            if (q.isBlank()) {
                flowOf(SearchState.Idle)
            } else {
                val ftsQuery = formatFtsQuery(q)
                val category = _selectedCategory.value
                val searchFlow = if (category != null) {
                    vaultItemRepository.searchByTypeFts(vaultId, category, ftsQuery)
                } else {
                    vaultItemRepository.searchItemsFts(vaultId, ftsQuery)
                }
                kotlinx.coroutines.flow.flow<SearchState> {
                    emit(SearchState.Loading(q))
                    emitAll(searchFlow.map { items: List<VaultItem> ->
                        if (items.isEmpty()) SearchState.Empty
                        else SearchState.Results(items, q)
                    })
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchState.Idle)

    val recentSearches: StateFlow<List<SearchHistory>> =
        searchHistoryRepository.getRecentSearches(vaultId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<SearchUiState> = combineFlows(
        _query, searchState, recentSearches, _selectedCategory
    ) { q, state, recent, category ->
        SearchUiState(
            query = q,
            searchState = state,
            recentSearches = recent,
            selectedCategory = category
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    fun onQueryChanged(query: String) {
        _query.value = query
    }

    fun onCategorySelected(category: VaultItemCategory?) {
        _selectedCategory.value = category
        val currentQuery = _query.value
        if (currentQuery.isNotBlank()) {
            _query.value = currentQuery
        }
    }

    fun onRecentSearchTapped(query: String) {
        _query.value = query
        recordSearch(query)
    }

    fun onSearchExecuted(query: String) {
        recordSearch(query)
    }

    fun onDeleteRecentSearch(id: String) {
        viewModelScope.launch {
            searchHistoryRepository.deleteSearch(id)
        }
    }

    fun onClearRecentSearches() {
        viewModelScope.launch {
            searchHistoryRepository.deleteAllForVault(vaultId)
        }
    }

    private fun recordSearch(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            when (searchHistoryRepository.recordSearch(vaultId, query.trim())) {
                is AppResult.Success -> {}
                is AppResult.Error -> {}
            }
        }
    }

    private fun formatFtsQuery(input: String): String {
        return input.trim()
            .replace(Regex("[*\"()^$\\[\\]]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { "${it}*" }
    }

    private fun <T1, T2, T3, T4, R> combineFlows(
        flow1: kotlinx.coroutines.flow.Flow<T1>,
        flow2: kotlinx.coroutines.flow.Flow<T2>,
        flow3: kotlinx.coroutines.flow.Flow<T3>,
        flow4: kotlinx.coroutines.flow.Flow<T4>,
        transform: suspend (T1, T2, T3, T4) -> R
    ): kotlinx.coroutines.flow.Flow<R> {
        return kotlinx.coroutines.flow.combine(flow1, flow2, flow3, flow4) { a, b, c, d ->
            transform(a, b, c, d)
        }
    }
}
