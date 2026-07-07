package com.passgo.app.core.database

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DatabaseMigrationTest {

    @Test
    fun `MIGRATION_3_4 creates custom_fields table`() {
        val migration = DatabaseMigrations.MIGRATION_3_4
        assertEquals(3, migration.startVersion)
        assertEquals(4, migration.endVersion)
    }

    @Test
    fun `MIGRATION_4_5 creates FTS table`() {
        val migration = DatabaseMigrations.MIGRATION_4_5
        assertEquals(4, migration.startVersion)
        assertEquals(5, migration.endVersion)
    }

    @Test
    fun `all migrations are registered in order`() {
        val migrations = DatabaseMigrations.ALL_MIGRATIONS
        assertEquals(5, migrations.size)
        assertEquals(1, migrations[0].startVersion)
        assertEquals(2, migrations[0].endVersion)
        assertEquals(2, migrations[1].startVersion)
        assertEquals(3, migrations[1].endVersion)
        assertEquals(3, migrations[2].startVersion)
        assertEquals(4, migrations[2].endVersion)
        assertEquals(4, migrations[3].startVersion)
        assertEquals(5, migrations[3].endVersion)
        assertEquals(5, migrations[4].startVersion)
        assertEquals(6, migrations[4].endVersion)
    }

    @Test
    fun `migration versions are sequential without gaps`() {
        for ((index, migration) in DatabaseMigrations.ALL_MIGRATIONS.withIndex()) {
            assertEquals(index + 1, migration.startVersion)
            assertEquals(index + 2, migration.endVersion)
        }
    }

    @Test
    fun `MIGRATION_3_4 SQL is non-empty`() {
        assertNotNull(DatabaseMigrations.MIGRATION_3_4)
    }

    @Test
    fun `MIGRATION_4_5 SQL is non-empty`() {
        assertNotNull(DatabaseMigrations.MIGRATION_4_5)
    }

    @Test
    fun `MIGRATION_5_6 creates search_history table`() {
        val migration = DatabaseMigrations.MIGRATION_5_6
        assertEquals(5, migration.startVersion)
        assertEquals(6, migration.endVersion)
    }

    @Test
    fun `MIGRATION_5_6 SQL is non-empty`() {
        assertNotNull(DatabaseMigrations.MIGRATION_5_6)
    }
}
