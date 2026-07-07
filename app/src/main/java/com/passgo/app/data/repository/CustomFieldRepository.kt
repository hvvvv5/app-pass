package com.passgo.app.data.repository

import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.CustomField
import kotlinx.coroutines.flow.Flow

interface CustomFieldRepository {
    fun getFieldsForItem(itemId: String): Flow<List<CustomField>>
    suspend fun getFieldsForItems(itemIds: List<String>): AppResult<List<CustomField>>
    suspend fun saveField(field: CustomField): AppResult<Unit>
    suspend fun saveFields(fields: List<CustomField>): AppResult<Unit>
    suspend fun deleteField(id: String): AppResult<Unit>
    suspend fun deleteAllForItem(itemId: String): AppResult<Unit>
}
