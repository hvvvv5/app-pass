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

    val MIGRATION_4_5 = Migration(4, 5) { db ->
        db.execSQL("""
            CREATE TRIGGER IF NOT EXISTS items_fts_insert AFTER INSERT ON vault_items BEGIN
                INSERT INTO items_fts(item_id, name, username, email, url, notes, custom_values)
                VALUES (new.id, new.name, new.username, new.email, new.url, new.notes, '');
            END
        """)

        db.execSQL("""
            CREATE TRIGGER IF NOT EXISTS items_fts_delete AFTER DELETE ON vault_items BEGIN
                DELETE FROM items_fts WHERE item_id = old.id;
            END
        """)

        db.execSQL("""
            CREATE TRIGGER IF NOT EXISTS items_fts_update AFTER UPDATE ON vault_items BEGIN
                DELETE FROM items_fts WHERE item_id = old.id;
                INSERT INTO items_fts(item_id, name, username, email, url, notes, custom_values)
                VALUES (new.id, new.name, new.username, new.email, new.url, new.notes, '');
            END
        """)

        db.execSQL("""
            CREATE TRIGGER IF NOT EXISTS items_fts_cf_insert AFTER INSERT ON custom_fields BEGIN
                UPDATE items_fts SET custom_values = (
                    SELECT COALESCE(group_concat(field_value, ' '), '') FROM custom_fields WHERE item_id = new.item_id
                ) WHERE item_id = new.item_id;
            END
        """)

        db.execSQL("""
            CREATE TRIGGER IF NOT EXISTS items_fts_cf_delete AFTER DELETE ON custom_fields BEGIN
                UPDATE items_fts SET custom_values = (
                    SELECT COALESCE(group_concat(field_value, ' '), '') FROM custom_fields WHERE item_id = old.item_id
                ) WHERE item_id = old.item_id;
            END
        """)

        db.execSQL("""
            CREATE TRIGGER IF NOT EXISTS items_fts_cf_update AFTER UPDATE ON custom_fields BEGIN
                UPDATE items_fts SET custom_values = (
                    SELECT COALESCE(group_concat(field_value, ' '), '') FROM custom_fields WHERE item_id = new.item_id
                ) WHERE item_id = new.item_id;
            END
        """)

        db.execSQL("""
            INSERT INTO items_fts(item_id, name, username, email, url, notes, custom_values)
            SELECT vi.id, vi.name, vi.username, vi.email, vi.url, vi.notes,
                COALESCE((SELECT group_concat(cf.field_value, ' ') FROM custom_fields cf WHERE cf.item_id = vi.id), '')
            FROM vault_items vi
        """)
    }

    val MIGRATION_5_6 = Migration(5, 6) { db ->
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS search_history (
                id TEXT NOT NULL PRIMARY KEY,
                vault_id TEXT NOT NULL,
                query TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_search_history_vault_created ON search_history(vault_id, created_at)")
    }

    val MIGRATION_6_7 = Migration(6, 7) { db ->
        db.execSQL("ALTER TABLE attachments ADD COLUMN encryption_iv BLOB NOT NULL DEFAULT x''")
        db.execSQL("ALTER TABLE attachments ADD COLUMN content_hash TEXT NOT NULL DEFAULT ''")
    }

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
}
