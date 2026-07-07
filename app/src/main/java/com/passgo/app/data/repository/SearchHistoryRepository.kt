package com.passgo.app.data.repository

import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.SearchHistory
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun getRecentSearches(vaultId: String): Flow<List<SearchHistory>>
    suspend fun recordSearch(vaultId: String, query: String): AppResult<Unit>
    suspend fun deleteSearch(id: String): AppResult<Unit>
    suspend fun deleteAllForVault(vaultId: String): AppResult<Unit>
}
