package com.passgo.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vault_items",
    foreignKeys = [
        ForeignKey(
            entity = VaultEntity::class,
            parentColumns = ["id"],
            childColumns = ["vault_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["vault_id"]),
        Index(value = ["folder_id"]),
        Index(value = ["type"]),
        Index(value = ["deleted_at"]),
        Index(value = ["archived_at"]),
        Index(value = ["favorite"]),
        Index(value = ["vault_id", "deleted_at", "archived_at"])
    ]
)
data class VaultItemEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "vault_id")
    val vaultId: String,
    @ColumnInfo(name = "folder_id")
    val folderId: String? = null,
    val type: String,
    val name: String,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val favorite: Boolean = false,
    @ColumnInfo(name = "archived_at")
    val archivedAt: Long? = null,
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
