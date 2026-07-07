package com.passgo.app.data.repository

import com.passgo.app.core.database.dao.SearchHistoryDao
import com.passgo.app.core.error.AppException
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.SearchHistory
import com.passgo.app.data.mapper.toDomain
import com.passgo.app.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao
) : SearchHistoryRepository {

    companion object {
        private const val MAX_HISTORY = 50
    }

    override fun getRecentSearches(vaultId: String): Flow<List<SearchHistory>> =
        searchHistoryDao.getRecentSearches(vaultId, MAX_HISTORY).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun recordSearch(vaultId: String, query: String): AppResult<Unit> = runCatching {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@runCatching

        val existingId = searchHistoryDao.getExistingQueryId(vaultId, trimmed)
        if (existingId != null) {
            searchHistoryDao.deleteById(existingId)
        }

        val search = SearchHistory(
            id = UUID.randomUUID().toString(),
            vaultId = vaultId,
            query = trimmed,
            createdAt = System.currentTimeMillis()
        )
        searchHistoryDao.insert(search.toEntity())

        val count = searchHistoryDao.getSearchCount(vaultId)
        if (count > MAX_HISTORY) {
            searchHistoryDao.deleteOldest(vaultId, MAX_HISTORY)
        }
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun deleteSearch(id: String): AppResult<Unit> = runCatching {
        searchHistoryDao.deleteById(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun deleteAllForVault(vaultId: String): AppResult<Unit> = runCatching {
        searchHistoryDao.deleteAllForVault(vaultId)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )
}
