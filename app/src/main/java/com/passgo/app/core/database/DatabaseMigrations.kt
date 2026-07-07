package com.passgo.app.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    val MIGRATION_1_2 = Migration(1, 2) { db ->
        db.execSQL("ALTER TABLE vault_items ADD COLUMN email TEXT NOT NULL DEFAULT ''")
    }

    val MIGRATION_2_3 = Migration(2, 3) { db ->
        db.execSQL("ALTER TABLE vault_items ADD COLUMN archived_at INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_vault_items_archived_at ON vault_items(archived_at)")
    }

    val MIGRATION_3_4 = Migration(3, 4) { db ->
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS custom_fields (
                id TEXT NOT NULL PRIMARY KEY,
                item_id TEXT NOT NULL,
                field_id TEXT NOT NULL,
                field_value TEXT NOT NULL DEFAULT '',
                sort_order INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (item_id) REFERENCES vault_items(id) ON DELETE CASCADE
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_custom_fields_item_id ON custom_fields(item_id)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_custom_fields_item_field ON custom_fields(item_id, field_id)")
    }

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
}
