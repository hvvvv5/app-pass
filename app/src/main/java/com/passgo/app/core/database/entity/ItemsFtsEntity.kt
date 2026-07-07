package com.passgo.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Fts4
@Entity(tableName = "items_fts")
data class ItemsFtsEntity(
    @PrimaryKey val rowid: Int = 0,
    @ColumnInfo(name = "item_id") val itemId: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val url: String = "",
    val notes: String = "",
    @ColumnInfo(name = "custom_values") val customValues: String = ""
)
