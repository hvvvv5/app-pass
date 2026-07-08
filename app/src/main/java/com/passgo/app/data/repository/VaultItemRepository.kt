package com.passgo.app.data.repository

import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.VaultItem
import com.passgo.app.core.model.VaultItemCategory
import kotlinx.coroutines.flow.Flow

interface VaultItemRepository {
    fun getActiveItems(vaultId: String): Flow<List<VaultItem>>
    fun getItemById(id: String): Flow<VaultItem?>
    fun getByType(vaultId: String, category: VaultItemCategory): Flow<List<VaultItem>>
    fun getByFolder(folderId: String): Flow<List<VaultItem>>
    fun getFavorites(vaultId: String): Flow<List<VaultItem>>
    fun getArchivedItems(vaultId: String): Flow<List<VaultItem>>
    fun getDeleted(vaultId: String): Flow<List<VaultItem>>
    fun getRecentItems(vaultId: String): Flow<List<VaultItem>>
    fun getItemsByTags(vaultId: String, tagIds: List<String>): Flow<List<VaultItem>>
    fun searchItems(vaultId: String, query: String): Flow<List<VaultItem>>
    fun searchByType(vaultId: String, type: VaultItemCategory, query: String): Flow<List<VaultItem>>
    fun searchFavorites(vaultId: String, query: String): Flow<List<VaultItem>>
    fun searchByFolder(vaultId: String, folderId: String, query: String): Flow<List<VaultItem>>
    fun getActiveItemsSortedByName(vaultId: String): Flow<List<VaultItem>>
    fun getActiveItemsSortedByNewest(vaultId: String): Flow<List<VaultItem>>
    fun getActiveItemsSortedByFavorite(vaultId: String): Flow<List<VaultItem>>
    suspend fun insert(item: VaultItem): AppResult<Unit>
    suspend fun insertBatch(items: List<VaultItem>): AppResult<Unit>
    suspend fun update(item: VaultItem): AppResult<Unit>
    suspend fun softDelete(id: String): AppResult<Unit>
    suspend fun restore(id: String): AppResult<Unit>
    suspend fun permanentDelete(id: String): AppResult<Unit>
    suspend fun archive(id: String): AppResult<Unit>
    suspend fun unarchive(id: String): AppResult<Unit>
    suspend fun moveItem(itemId: String, folderId: String?): AppResult<Unit>
    suspend fun permanentDeleteOldTrash(threshold: Long): AppResult<Unit>

    fun searchItemsFts(vaultId: String, query: String): Flow<List<VaultItem>>
    fun searchByTypeFts(vaultId: String, type: VaultItemCategory, query: String): Flow<List<VaultItem>>
    fun searchFavoritesFts(vaultId: String, query: String): Flow<List<VaultItem>>
    fun searchByFolderFts(vaultId: String, folderId: String, query: String): Flow<List<VaultItem>>

    // Paginated variants
    fun getActiveItemsPaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun getByTypePaged(vaultId: String, category: VaultItemCategory, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun getByFolderPaged(folderId: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun getFavoritesPaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun getArchivedItemsPaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun getDeletedPaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun searchItemsPaged(vaultId: String, query: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun searchByTypePaged(vaultId: String, type: VaultItemCategory, query: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun searchFavoritesPaged(vaultId: String, query: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun searchByFolderPaged(vaultId: String, folderId: String, query: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun getActiveItemsSortedByNamePaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun getActiveItemsSortedByNewestPaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun getActiveItemsSortedByFavoritePaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun getRecentItemsPaged(vaultId: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun getItemsByTagsPaged(vaultId: String, tagIds: List<String>, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun searchItemsFtsPaged(vaultId: String, query: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun searchByTypeFtsPaged(vaultId: String, type: VaultItemCategory, query: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun searchFavoritesFtsPaged(vaultId: String, query: String, limit: Int, offset: Int): Flow<List<VaultItem>>
    fun searchByFolderFtsPaged(vaultId: String, folderId: String, query: String, limit: Int, offset: Int): Flow<List<VaultItem>>
}
