package com.passgo.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.passgo.app.core.database.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history WHERE vault_id = :vaultId ORDER BY created_at DESC LIMIT :limit")
    fun getRecentSearches(vaultId: String, limit: Int = 50): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM search_history WHERE vault_id = :vaultId")
    suspend fun deleteAllForVault(vaultId: String)

    @Query("SELECT COUNT(*) FROM search_history WHERE vault_id = :vaultId")
    suspend fun getSearchCount(vaultId: String): Int

    @Query("DELETE FROM search_history WHERE vault_id = :vaultId AND id NOT IN (SELECT id FROM search_history WHERE vault_id = :vaultId ORDER BY created_at DESC LIMIT :keepCount)")
    suspend fun deleteOldest(vaultId: String, keepCount: Int)

    @Query("SELECT id FROM search_history WHERE vault_id = :vaultId AND query = :query LIMIT 1")
    suspend fun getExistingQueryId(vaultId: String, query: String): String?
}
