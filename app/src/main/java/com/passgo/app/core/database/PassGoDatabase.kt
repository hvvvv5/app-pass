package com.passgo.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.passgo.app.core.database.dao.AttachmentDao
import com.passgo.app.core.database.dao.CustomFieldDao
import com.passgo.app.core.database.dao.FolderDao
import com.passgo.app.core.database.dao.SearchHistoryDao
import com.passgo.app.core.database.dao.TagDao
import com.passgo.app.core.database.dao.VaultDao
import com.passgo.app.core.database.dao.VaultItemDao
import com.passgo.app.core.database.entity.AttachmentEntity
import com.passgo.app.core.database.entity.CustomFieldEntity
import com.passgo.app.core.database.entity.FolderEntity
import com.passgo.app.core.database.entity.ItemsFtsEntity
import com.passgo.app.core.database.entity.SearchHistoryEntity
import com.passgo.app.core.database.entity.TagEntity
import com.passgo.app.core.database.entity.TagItemCrossRef
import com.passgo.app.core.database.entity.VaultEntity
import com.passgo.app.core.database.entity.VaultItemEntity
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        VaultEntity::class,
        VaultItemEntity::class,
        FolderEntity::class,
        TagEntity::class,
        TagItemCrossRef::class,
        AttachmentEntity::class,
        CustomFieldEntity::class,
        ItemsFtsEntity::class,
        SearchHistoryEntity::class
    ],
    version = 8,
    exportSchema = true
)
abstract class PassGoDatabase : RoomDatabase() {

    abstract fun vaultDao(): VaultDao
    abstract fun vaultItemDao(): VaultItemDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun customFieldDao(): CustomFieldDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        private const val DB_NAME = "passgo_vault.db"
        private const val SQLCIPHER_OPTIONS = "PRAGMA cipher_compatibility = 4"

        val ftsCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                createFtsTriggers(db)
            }
        }

        fun createFtsTriggers(db: SupportSQLiteDatabase) {
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
        }

        fun build(context: Context, passphrase: ByteArray): PassGoDatabase {
            val factory = object : SupportSQLiteOpenHelper.Factory {
                override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
                    val sqlCipherFactory = SupportOpenHelperFactory(passphrase)
                    val helper = sqlCipherFactory.create(configuration)
                    helper.writableDatabase.execSQL(SQLCIPHER_OPTIONS)
                    passphrase.fill(0)
                    return helper
                }
            }

            return Room.databaseBuilder(
                context.applicationContext,
                PassGoDatabase::class.java,
                DB_NAME
            )
                .openHelperFactory(factory)
                .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
                .addCallback(ftsCallback)
                .build()
        }



        fun buildInMemory(context: Context, passphrase: ByteArray): PassGoDatabase {
            val factory = SupportOpenHelperFactory(passphrase)
            val db = Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                PassGoDatabase::class.java
            )
                .openHelperFactory(factory)
                .addCallback(ftsCallback)
                .build()
            passphrase.fill(0)
            return db
        }
    }
}
