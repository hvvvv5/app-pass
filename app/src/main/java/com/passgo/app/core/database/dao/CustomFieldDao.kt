package com.passgo.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.passgo.app.core.database.entity.CustomFieldEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomFieldDao {

    @Query("SELECT * FROM custom_fields WHERE item_id = :itemId ORDER BY sort_order ASC")
    fun getFieldsForItem(itemId: String): Flow<List<CustomFieldEntity>>

    @Query("SELECT * FROM custom_fields WHERE item_id IN (:itemIds) ORDER BY sort_order ASC")
    suspend fun getFieldsForItems(itemIds: List<String>): List<CustomFieldEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(field: CustomFieldEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(fields: List<CustomFieldEntity>)

    @Query("DELETE FROM custom_fields WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM custom_fields WHERE item_id = :itemId")
    suspend fun deleteAllForItem(itemId: String)
}
