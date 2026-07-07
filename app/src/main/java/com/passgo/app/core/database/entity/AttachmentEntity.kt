package com.passgo.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = VaultItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["item_id"]),
        Index(value = ["deleted_at"])
    ]
)
data class AttachmentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "item_id")
    val itemId: String,
    val name: String,
    @ColumnInfo(name = "mime_type")
    val mimeType: String = "application/octet-stream",
    @ColumnInfo(name = "encrypted_file_uri")
    val encryptedFileUri: String = "",
    @ColumnInfo(name = "encryption_iv")
    val encryptionIv: ByteArray = byteArrayOf(),
    @ColumnInfo(name = "content_hash")
    val contentHash: String = "",
    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,
    @ColumnInfo(name = "sync_version")
    val syncVersion: Int = 0,
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "SYNCED"
)
