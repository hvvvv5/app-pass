package com.passgo.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passgo.app.core.database.entity.VaultItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultItemDao {

    @Query("SELECT COUNT(*) FROM vault_items WHERE deleted_at IS NULL AND archived_at IS NULL")
    fun getActiveItemsCount(): Flow<Int>

    @Query("SELECT * FROM vault_items WHERE id = :id")
    fun getItemById(id: String): Flow<VaultItemEntity?>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL ORDER BY updated_at DESC")
    fun getActiveItems(vaultId: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL AND type = :type ORDER BY updated_at DESC")
    fun getByType(vaultId: String, type: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE deleted_at IS NULL AND archived_at IS NULL AND folder_id = :folderId ORDER BY updated_at DESC")
    fun getByFolder(folderId: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL AND favorite = 1 ORDER BY updated_at DESC")
    fun getFavorites(vaultId: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND archived_at IS NOT NULL AND deleted_at IS NULL ORDER BY archived_at DESC")
    fun getArchivedItems(vaultId: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NOT NULL ORDER BY deleted_at DESC")
    fun getDeleted(vaultId: String): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT DISTINCT vi.* FROM vault_items vi
        LEFT JOIN custom_fields cf ON vi.id = cf.item_id
        WHERE vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL 
        AND (vi.name LIKE '%' || :query || '%' OR vi.username LIKE '%' || :query || '%' OR vi.email LIKE '%' || :query || '%' OR vi.url LIKE '%' || :query || '%' OR vi.notes LIKE '%' || :query || '%' OR cf.field_value LIKE '%' || :query || '%')
        ORDER BY vi.updated_at DESC
    """)
    fun searchItems(vaultId: String, query: String): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT DISTINCT vi.* FROM vault_items vi
        LEFT JOIN custom_fields cf ON vi.id = cf.item_id
        WHERE vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL AND vi.type = :type
        AND (vi.name LIKE '%' || :query || '%' OR vi.username LIKE '%' || :query || '%' OR vi.email LIKE '%' || :query || '%' OR vi.url LIKE '%' || :query || '%' OR vi.notes LIKE '%' || :query || '%' OR cf.field_value LIKE '%' || :query || '%')
        ORDER BY vi.updated_at DESC
    """)
    fun searchByType(vaultId: String, type: String, query: String): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT DISTINCT vi.* FROM vault_items vi
        LEFT JOIN custom_fields cf ON vi.id = cf.item_id
        WHERE vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL AND vi.favorite = 1
        AND (vi.name LIKE '%' || :query || '%' OR vi.username LIKE '%' || :query || '%' OR vi.email LIKE '%' || :query || '%' OR vi.url LIKE '%' || :query || '%' OR vi.notes LIKE '%' || :query || '%' OR cf.field_value LIKE '%' || :query || '%')
        ORDER BY vi.updated_at DESC
    """)
    fun searchFavorites(vaultId: String, query: String): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT DISTINCT vi.* FROM vault_items vi
        LEFT JOIN custom_fields cf ON vi.id = cf.item_id
        WHERE vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL AND vi.folder_id = :folderId
        AND (vi.name LIKE '%' || :query || '%' OR vi.username LIKE '%' || :query || '%' OR vi.email LIKE '%' || :query || '%' OR vi.url LIKE '%' || :query || '%' OR vi.notes LIKE '%' || :query || '%' OR cf.field_value LIKE '%' || :query || '%')
        ORDER BY vi.updated_at DESC
    """)
    fun searchByFolder(vaultId: String, folderId: String, query: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL ORDER BY name ASC")
    fun getActiveItemsSortedByName(vaultId: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL ORDER BY created_at DESC")
    fun getActiveItemsSortedByNewest(vaultId: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL ORDER BY favorite DESC, updated_at DESC")
    fun getActiveItemsSortedByFavorite(vaultId: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL ORDER BY updated_at DESC LIMIT 20")
    fun getRecentItems(vaultId: String): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT vi.* FROM vault_items vi
        INNER JOIN tag_item ti ON vi.id = ti.item_id
        WHERE vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL
        AND ti.tag_id IN (:tagIds)
        GROUP BY vi.id
        HAVING COUNT(DISTINCT ti.tag_id) = :tagCount
        ORDER BY vi.updated_at DESC
    """)
    fun getItemsByTags(vaultId: String, tagIds: List<String>, tagCount: Int): Flow<List<VaultItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VaultItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(items: List<VaultItemEntity>)

    @Update
    suspend fun update(item: VaultItemEntity)

    @Query("UPDATE vault_items SET deleted_at = :timestamp, sync_status = 'PENDING_UPDATE' WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE vault_items SET deleted_at = NULL, sync_status = 'PENDING_UPDATE' WHERE id = :id")
    suspend fun restore(id: String)

    @Query("UPDATE vault_items SET archived_at = :timestamp, sync_status = 'PENDING_UPDATE' WHERE id = :id")
    suspend fun archive(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE vault_items SET archived_at = NULL, sync_status = 'PENDING_UPDATE' WHERE id = :id")
    suspend fun unarchive(id: String)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun permanentDelete(id: String)

    @Query("UPDATE vault_items SET folder_id = :folderId, sync_status = 'PENDING_UPDATE' WHERE id = :itemId")
    suspend fun moveItem(itemId: String, folderId: String?)

    @Query("DELETE FROM vault_items WHERE deleted_at IS NOT NULL AND deleted_at < :threshold")
    suspend fun permanentDeleteOldTrash(threshold: Long)

    @Query("SELECT COUNT(*) FROM vault_items WHERE deleted_at IS NOT NULL AND deleted_at < :threshold")
    fun getOldTrashCount(threshold: Long): Flow<Int>

    @Query("""
        SELECT vi.* FROM vault_items vi
        INNER JOIN items_fts fts ON vi.id = fts.item_id
        WHERE items_fts MATCH :query
        AND vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL
    """)
    fun searchItemsFts(vaultId: String, query: String): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT vi.* FROM vault_items vi
        INNER JOIN items_fts fts ON vi.id = fts.item_id
        WHERE items_fts MATCH :query
        AND vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL
        AND vi.type = :type
    """)
    fun searchByTypeFts(vaultId: String, type: String, query: String): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT vi.* FROM vault_items vi
        INNER JOIN items_fts fts ON vi.id = fts.item_id
        WHERE items_fts MATCH :query
        AND vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL
        AND vi.favorite = 1
    """)
    fun searchFavoritesFts(vaultId: String, query: String): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT vi.* FROM vault_items vi
        INNER JOIN items_fts fts ON vi.id = fts.item_id
        WHERE items_fts MATCH :query
        AND vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL
        AND vi.folder_id = :folderId
    """)
    fun searchByFolderFts(vaultId: String, folderId: String, query: String): Flow<List<VaultItemEntity>>

    // Paginated variants
    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL ORDER BY updated_at DESC LIMIT :limit OFFSET :offset")
    fun getActiveItemsPaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL AND type = :type ORDER BY updated_at DESC LIMIT :limit OFFSET :offset")
    fun getByTypePaged(vaultId: String, type: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE deleted_at IS NULL AND archived_at IS NULL AND folder_id = :folderId ORDER BY updated_at DESC LIMIT :limit OFFSET :offset")
    fun getByFolderPaged(folderId: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL AND favorite = 1 ORDER BY updated_at DESC LIMIT :limit OFFSET :offset")
    fun getFavoritesPaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND archived_at IS NOT NULL AND deleted_at IS NULL ORDER BY archived_at DESC LIMIT :limit OFFSET :offset")
    fun getArchivedItemsPaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NOT NULL ORDER BY deleted_at DESC LIMIT :limit OFFSET :offset")
    fun getDeletedPaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT DISTINCT vi.* FROM vault_items vi
        LEFT JOIN custom_fields cf ON vi.id = cf.item_id
        WHERE vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL 
        AND (vi.name LIKE '%' || :query || '%' OR vi.username LIKE '%' || :query || '%' OR vi.email LIKE '%' || :query || '%' OR vi.url LIKE '%' || :query || '%' OR vi.notes LIKE '%' || :query || '%' OR cf.field_value LIKE '%' || :query || '%')
        ORDER BY vi.updated_at DESC LIMIT :limit OFFSET :offset
    """)
    fun searchItemsPaged(vaultId: String, query: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT DISTINCT vi.* FROM vault_items vi
        LEFT JOIN custom_fields cf ON vi.id = cf.item_id
        WHERE vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL AND vi.type = :type
        AND (vi.name LIKE '%' || :query || '%' OR vi.username LIKE '%' || :query || '%' OR vi.email LIKE '%' || :query || '%' OR vi.url LIKE '%' || :query || '%' OR vi.notes LIKE '%' || :query || '%' OR cf.field_value LIKE '%' || :query || '%')
        ORDER BY vi.updated_at DESC LIMIT :limit OFFSET :offset
    """)
    fun searchByTypePaged(vaultId: String, type: String, query: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT DISTINCT vi.* FROM vault_items vi
        LEFT JOIN custom_fields cf ON vi.id = cf.item_id
        WHERE vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL AND vi.favorite = 1
        AND (vi.name LIKE '%' || :query || '%' OR vi.username LIKE '%' || :query || '%' OR vi.email LIKE '%' || :query || '%' OR vi.url LIKE '%' || :query || '%' OR vi.notes LIKE '%' || :query || '%' OR cf.field_value LIKE '%' || :query || '%')
        ORDER BY vi.updated_at DESC LIMIT :limit OFFSET :offset
    """)
    fun searchFavoritesPaged(vaultId: String, query: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT DISTINCT vi.* FROM vault_items vi
        LEFT JOIN custom_fields cf ON vi.id = cf.item_id
        WHERE vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL AND vi.folder_id = :folderId
        AND (vi.name LIKE '%' || :query || '%' OR vi.username LIKE '%' || :query || '%' OR vi.email LIKE '%' || :query || '%' OR vi.url LIKE '%' || :query || '%' OR vi.notes LIKE '%' || :query || '%' OR cf.field_value LIKE '%' || :query || '%')
        ORDER BY vi.updated_at DESC LIMIT :limit OFFSET :offset
    """)
    fun searchByFolderPaged(vaultId: String, folderId: String, query: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL ORDER BY name ASC LIMIT :limit OFFSET :offset")
    fun getActiveItemsSortedByNamePaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun getActiveItemsSortedByNewestPaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL ORDER BY favorite DESC, updated_at DESC LIMIT :limit OFFSET :offset")
    fun getActiveItemsSortedByFavoritePaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE vault_id = :vaultId AND deleted_at IS NULL AND archived_at IS NULL ORDER BY updated_at DESC LIMIT :limit OFFSET :offset")
    fun getRecentItemsPaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT vi.* FROM vault_items vi
        INNER JOIN tag_item ti ON vi.id = ti.item_id
        WHERE vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL
        AND ti.tag_id IN (:tagIds)
        GROUP BY vi.id
        HAVING COUNT(DISTINCT ti.tag_id) = :tagCount
        ORDER BY vi.updated_at DESC LIMIT :limit OFFSET :offset
    """)
    fun getItemsByTagsPaged(vaultId: String, tagIds: List<String>, tagCount: Int, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT vi.* FROM vault_items vi
        INNER JOIN items_fts fts ON vi.id = fts.item_id
        WHERE items_fts MATCH :query
        AND vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL
        LIMIT :limit OFFSET :offset
    """)
    fun searchItemsFtsPaged(vaultId: String, query: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT vi.* FROM vault_items vi
        INNER JOIN items_fts fts ON vi.id = fts.item_id
        WHERE items_fts MATCH :query
        AND vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL
        AND vi.type = :type LIMIT :limit OFFSET :offset
    """)
    fun searchByTypeFtsPaged(vaultId: String, type: String, query: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT vi.* FROM vault_items vi
        INNER JOIN items_fts fts ON vi.id = fts.item_id
        WHERE items_fts MATCH :query
        AND vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL
        AND vi.favorite = 1 LIMIT :limit OFFSET :offset
    """)
    fun searchFavoritesFtsPaged(vaultId: String, query: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>

    @Query("""
        SELECT vi.* FROM vault_items vi
        INNER JOIN items_fts fts ON vi.id = fts.item_id
        WHERE items_fts MATCH :query
        AND vi.vault_id = :vaultId AND vi.deleted_at IS NULL AND vi.archived_at IS NULL
        AND vi.folder_id = :folderId LIMIT :limit OFFSET :offset
    """)
    fun searchByFolderFtsPaged(vaultId: String, folderId: String, query: String, limit: Int, offset: Int): Flow<List<VaultItemEntity>>
}
