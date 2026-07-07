package com.passgo.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passgo.app.core.database.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachments WHERE item_id = :itemId AND deleted_at IS NULL ORDER BY created_at ASC")
    fun getAttachmentsForItem(itemId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE id = :id")
    fun getAttachmentById(id: String): Flow<AttachmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: AttachmentEntity)

    @Update
    suspend fun update(attachment: AttachmentEntity)

    @Query("UPDATE attachments SET deleted_at = :timestamp, sync_status = 'PENDING_UPDATE' WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun permanentDelete(id: String)
}
