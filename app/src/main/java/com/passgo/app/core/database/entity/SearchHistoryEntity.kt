package com.passgo.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_history",
    indices = [
        Index(value = ["vault_id", "created_at"])
    ]
)
data class SearchHistoryEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "vault_id")
    val vaultId: String,
    val query: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
