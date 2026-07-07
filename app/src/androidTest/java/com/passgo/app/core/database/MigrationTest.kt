package com.passgo.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.security.SecureRandom

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val passphrase = ByteArray(32).apply { SecureRandom().nextBytes(this) }
    private val factory = SupportOpenHelperFactory(passphrase)

    @After
    fun tearDown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("migration_test.db")
    }

    @Test
    fun databaseCreation_createsAllTables() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.databaseBuilder<PassGoDatabase>(
            context,
            PassGoDatabase::class.java,
            "migration_test.db"
        )
            .openHelperFactory(factory)
            .build()

        db.openDb()
        val database = db.getOpenHelper().readableDatabase

        val cursor = database.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'room_%' AND name NOT LIKE 'android_%' AND name NOT LIKE 'items_fts%'",
            null
        )
        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()

        assertTrue("vaults table missing", tables.contains("vaults"))
        assertTrue("vault_items table missing", tables.contains("vault_items"))
        assertTrue("folders table missing", tables.contains("folders"))
        assertTrue("tags table missing", tables.contains("tags"))
        assertTrue("tag_item table missing", tables.contains("tag_item"))
        assertTrue("attachments table missing", tables.contains("attachments"))
        assertTrue("search_history table missing", tables.contains("search_history"))

        db.close()
    }

    @Test
    fun databaseVersion_isSix() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.databaseBuilder<PassGoDatabase>(
            context,
            PassGoDatabase::class.java,
            "migration_test.db"
        )
            .openHelperFactory(factory)
            .build()

        val version = db.openHelper.readableDatabase.version
        assertTrue("Expected version >=6 but got $version", version >= 6)

        db.close()
    }

    @Test
    fun databaseCreation_createsItemsFts() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.databaseBuilder<PassGoDatabase>(
            context,
            PassGoDatabase::class.java,
            "migration_test.db"
        )
            .openHelperFactory(factory)
            .build()

        db.openDb()
        val database = db.getOpenHelper().readableDatabase

        val cursor = database.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'items_fts%'",
            null
        )
        val ftsTables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            ftsTables.add(cursor.getString(0))
        }
        cursor.close()

        assertTrue("items_fts table missing", ftsTables.contains("items_fts"))
        assertTrue("items_fts_content missing", ftsTables.contains("items_fts_content"))

        val triggerCursor = database.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='trigger' AND name LIKE 'items_fts_%'",
            null
        )
        val triggers = mutableListOf<String>()
        while (triggerCursor.moveToNext()) {
            triggers.add(triggerCursor.getString(0))
        }
        triggerCursor.close()

        assertTrue("items_fts_insert trigger missing", triggers.contains("items_fts_insert"))
        assertTrue("items_fts_delete trigger missing", triggers.contains("items_fts_delete"))
        assertTrue("items_fts_update trigger missing", triggers.contains("items_fts_update"))
        assertTrue("items_fts_cf_insert trigger missing", triggers.contains("items_fts_cf_insert"))
        assertTrue("items_fts_cf_delete trigger missing", triggers.contains("items_fts_cf_delete"))
        assertTrue("items_fts_cf_update trigger missing", triggers.contains("items_fts_cf_update"))

        db.close()
    }

    @Test
    fun itemsFts_triggerSyncsInsertedItem() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.databaseBuilder<PassGoDatabase>(
            context,
            PassGoDatabase::class.java,
            "migration_test.db"
        )
            .openHelperFactory(factory)
            .build()

        db.openDb()
        val database = db.getOpenHelper().writableDatabase

        database.execSQL("INSERT INTO vaults (id, name) VALUES ('v1', 'Test Vault')")
        database.execSQL("INSERT INTO vault_items (id, vault_id, type, name, username, email, url, notes) VALUES ('i1', 'v1', 'LOGIN', 'GitHub', 'user', 'user@test.com', 'https://github.com', 'My notes')")

        val cursor = database.rawQuery("SELECT item_id, name, username, email, url, notes FROM items_fts WHERE item_id = 'i1'", null)
        assertTrue("FTS row should exist after insert", cursor.moveToFirst())
        assertEqualsString("GitHub", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEqualsString("user", cursor.getString(cursor.getColumnIndexOrThrow("username")))
        cursor.close()

        db.close()
    }

    @Test
    fun itemsFts_triggerSyncsDeletedItem() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.databaseBuilder<PassGoDatabase>(
            context,
            PassGoDatabase::class.java,
            "migration_test.db"
        )
            .openHelperFactory(factory)
            .build()

        db.openDb()
        val database = db.getOpenHelper().writableDatabase

        database.execSQL("INSERT INTO vaults (id, name) VALUES ('v2', 'Test Vault')")
        database.execSQL("INSERT INTO vault_items (id, vault_id, type, name) VALUES ('i2', 'v2', 'LOGIN', 'ToDelete')")
        database.execSQL("DELETE FROM vault_items WHERE id = 'i2'")

        val cursor = database.rawQuery("SELECT COUNT(*) FROM items_fts WHERE item_id = 'i2'", null)
        cursor.moveToFirst()
        assertEqualsString("0", cursor.getString(0))
        cursor.close()

        db.close()
    }

    @Test
    fun itemsFts_ftsMatchReturnsResults() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.databaseBuilder<PassGoDatabase>(
            context,
            PassGoDatabase::class.java,
            "migration_test.db"
        )
            .openHelperFactory(factory)
            .build()

        db.openDb()
        val database = db.getOpenHelper().writableDatabase

        database.execSQL("INSERT INTO vaults (id, name) VALUES ('v3', 'Test Vault')")
        database.execSQL("INSERT INTO vault_items (id, vault_id, type, name, username, notes) VALUES ('i3', 'v3', 'LOGIN', 'GitHub Account', 'dev@github.com', 'My repository')")

        val cursor = database.rawQuery("SELECT item_id FROM items_fts WHERE items_fts MATCH 'github'", null)
        assertTrue("FTS MATCH should return results for 'github'", cursor.moveToFirst())
        assertEqualsString("i3", cursor.getString(0))
        cursor.close()

        db.close()
    }

    private fun assertEqualsString(expected: String, actual: String) {
        if (expected != actual) throw AssertionError("Expected <$expected> but was <$actual>")
    }
}
