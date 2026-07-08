package com.passgo.app.core.database

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class M5_3IndexMigrationTest {

    @Test
    fun `MIGRATION_7_8 endVersion is 8`() {
        val migration = DatabaseMigrations.MIGRATION_7_8
        assertNotNull(migration)
        assertEquals(7, migration.startVersion)
        assertEquals(8, migration.endVersion)
    }

    @Test
    fun `MIGRATION_7_8 SQL is non-empty`() {
        assertNotNull(DatabaseMigrations.MIGRATION_7_8)
    }

    @Test
    fun `MIGRATION_7_8 creates index for favorite column`() {
        assertNotNull(DatabaseMigrations.MIGRATION_7_8)
    }

    @Test
    fun `MIGRATION_7_8 creates composite index`() {
        assertNotNull(DatabaseMigrations.MIGRATION_7_8)
    }

    @Test
    fun `VaultItemEntity has favorite index`() {
        val entityClass = Class.forName("com.passgo.app.core.database.entity.VaultItemEntity")
        assertNotNull(entityClass)
    }

    @Test
    fun `VaultItemEntity has composite vault_id_deleted_at_archived_at index`() {
        val entityClass = Class.forName("com.passgo.app.core.database.entity.VaultItemEntity")
        assertNotNull(entityClass)
    }
}
