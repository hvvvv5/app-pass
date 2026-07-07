package com.passgo.app.data.repository

import com.passgo.app.core.database.dao.VaultItemDao
import com.passgo.app.core.error.AppException
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.VaultItem
import com.passgo.app.core.model.VaultItemCategory
import com.passgo.app.data.mapper.toDomain
import com.passgo.app.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultItemRepositoryImpl @Inject constructor(
    private val vaultItemDao: VaultItemDao
) : VaultItemRepository {

    override fun getActiveItems(vaultId: String): Flow<List<VaultItem>> =
        vaultItemDao.getActiveItems(vaultId).map { list -> list.map { it.toDomain() } }

    override fun getItemById(id: String): Flow<VaultItem?> =
        vaultItemDao.getItemById(id).map { it?.toDomain() }

    override fun getByType(vaultId: String, category: VaultItemCategory): Flow<List<VaultItem>> =
        vaultItemDao.getByType(vaultId, category.name).map { list -> list.map { it.toDomain() } }

    override fun getByFolder(folderId: String): Flow<List<VaultItem>> =
        vaultItemDao.getByFolder(folderId).map { list -> list.map { it.toDomain() } }

    override fun getFavorites(vaultId: String): Flow<List<VaultItem>> =
        vaultItemDao.getFavorites(vaultId).map { list -> list.map { it.toDomain() } }

    override fun getArchivedItems(vaultId: String): Flow<List<VaultItem>> =
        vaultItemDao.getArchivedItems(vaultId).map { list -> list.map { it.toDomain() } }

    override fun getDeleted(vaultId: String): Flow<List<VaultItem>> =
        vaultItemDao.getDeleted(vaultId).map { list -> list.map { it.toDomain() } }

    override fun getRecentItems(vaultId: String): Flow<List<VaultItem>> =
        vaultItemDao.getRecentItems(vaultId).map { list -> list.map { it.toDomain() } }

    override fun getItemsByTags(vaultId: String, tagIds: List<String>): Flow<List<VaultItem>> =
        vaultItemDao.getItemsByTags(vaultId, tagIds, tagIds.size).map { list -> list.map { it.toDomain() } }

    override fun searchItems(vaultId: String, query: String): Flow<List<VaultItem>> =
        vaultItemDao.searchItems(vaultId, query).map { list -> list.map { it.toDomain() } }

    override fun searchByType(vaultId: String, type: VaultItemCategory, query: String): Flow<List<VaultItem>> =
        vaultItemDao.searchByType(vaultId, type.name, query).map { list -> list.map { it.toDomain() } }

    override fun searchFavorites(vaultId: String, query: String): Flow<List<VaultItem>> =
        vaultItemDao.searchFavorites(vaultId, query).map { list -> list.map { it.toDomain() } }

    override fun searchByFolder(vaultId: String, folderId: String, query: String): Flow<List<VaultItem>> =
        vaultItemDao.searchByFolder(vaultId, folderId, query).map { list -> list.map { it.toDomain() } }

    override fun getActiveItemsSortedByName(vaultId: String): Flow<List<VaultItem>> =
        vaultItemDao.getActiveItemsSortedByName(vaultId).map { list -> list.map { it.toDomain() } }

    override fun getActiveItemsSortedByNewest(vaultId: String): Flow<List<VaultItem>> =
        vaultItemDao.getActiveItemsSortedByNewest(vaultId).map { list -> list.map { it.toDomain() } }

    override fun getActiveItemsSortedByFavorite(vaultId: String): Flow<List<VaultItem>> =
        vaultItemDao.getActiveItemsSortedByFavorite(vaultId).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(item: VaultItem): AppResult<Unit> = runCatching {
        vaultItemDao.insert(item.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun insertBatch(items: List<VaultItem>): AppResult<Unit> = runCatching {
        vaultItemDao.insertBatch(items.map { it.toEntity() })
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun update(item: VaultItem): AppResult<Unit> = runCatching {
        vaultItemDao.update(item.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun softDelete(id: String): AppResult<Unit> = runCatching {
        vaultItemDao.softDelete(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun restore(id: String): AppResult<Unit> = runCatching {
        vaultItemDao.restore(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun permanentDelete(id: String): AppResult<Unit> = runCatching {
        vaultItemDao.permanentDelete(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun archive(id: String): AppResult<Unit> = runCatching {
        vaultItemDao.archive(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun unarchive(id: String): AppResult<Unit> = runCatching {
        vaultItemDao.unarchive(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun moveItem(itemId: String, folderId: String?): AppResult<Unit> = runCatching {
        vaultItemDao.moveItem(itemId, folderId)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun permanentDeleteOldTrash(threshold: Long): AppResult<Unit> = runCatching {
        vaultItemDao.permanentDeleteOldTrash(threshold)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override fun searchItemsFts(vaultId: String, query: String): Flow<List<VaultItem>> =
        vaultItemDao.searchItemsFts(vaultId, query).map { list -> list.map { it.toDomain() } }

    override fun searchByTypeFts(vaultId: String, type: VaultItemCategory, query: String): Flow<List<VaultItem>> =
        vaultItemDao.searchByTypeFts(vaultId, type.name, query).map { list -> list.map { it.toDomain() } }

    override fun searchFavoritesFts(vaultId: String, query: String): Flow<List<VaultItem>> =
        vaultItemDao.searchFavoritesFts(vaultId, query).map { list -> list.map { it.toDomain() } }

    override fun searchByFolderFts(vaultId: String, folderId: String, query: String): Flow<List<VaultItem>> =
        vaultItemDao.searchByFolderFts(vaultId, folderId, query).map { list -> list.map { it.toDomain() } }
}
