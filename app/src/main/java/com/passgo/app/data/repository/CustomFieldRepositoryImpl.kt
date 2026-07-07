package com.passgo.app.data.repository

import com.passgo.app.core.database.dao.CustomFieldDao
import com.passgo.app.core.error.AppException
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.CustomField
import com.passgo.app.data.mapper.toDomain
import com.passgo.app.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomFieldRepositoryImpl @Inject constructor(
    private val customFieldDao: CustomFieldDao
) : CustomFieldRepository {

    override fun getFieldsForItem(itemId: String): Flow<List<CustomField>> =
        customFieldDao.getFieldsForItem(itemId).map { list -> list.map { it.toDomain() } }

    override suspend fun getFieldsForItems(itemIds: List<String>): AppResult<List<CustomField>> = runCatching {
        customFieldDao.getFieldsForItems(itemIds).map { it.toDomain() }
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun saveField(field: CustomField): AppResult<Unit> = runCatching {
        customFieldDao.insert(field.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun saveFields(fields: List<CustomField>): AppResult<Unit> = runCatching {
        customFieldDao.insertBatch(fields.map { it.toEntity() })
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun deleteField(id: String): AppResult<Unit> = runCatching {
        customFieldDao.delete(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun deleteAllForItem(itemId: String): AppResult<Unit> = runCatching {
        customFieldDao.deleteAllForItem(itemId)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )
}
