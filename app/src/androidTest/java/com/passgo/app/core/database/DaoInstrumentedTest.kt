package com.passgo.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.passgo.app.core.BaseInstrumentedTest
import com.passgo.app.core.database.dao.VaultDao
import com.passgo.app.core.database.dao.VaultItemDao
import com.passgo.app.core.database.entity.VaultEntity
import com.passgo.app.core.database.entity.VaultItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DaoInstrumentedTest : BaseInstrumentedTest() {

    private lateinit var database: PassGoDatabase
    private lateinit var vaultDao: VaultDao
    private lateinit var vaultItemDao: VaultItemDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val passphrase = ByteArray(32).apply { java.security.SecureRandom().nextBytes(this) }
        database = PassGoDatabase.buildInMemory(context, passphrase)
        vaultDao = database.vaultDao()
        vaultItemDao = database.vaultItemDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun vaultDao_insertAndQuery() = runBlocking {
        val vault = VaultEntity(
            id = UUID.randomUUID().toString(),
            name = "Test Vault",
            description = "Test Description"
        )
        vaultDao.insert(vault)

        val result = vaultDao.getActiveVault().first()
        assertNotNull(result)
        assertEquals(vault.name, result!!.name)
    }

    @Test
    fun vaultDao_softDelete() = runBlocking {
        val vault = VaultEntity(
            id = UUID.randomUUID().toString(),
            name = "Deletable Vault"
        )
        vaultDao.insert(vault)
        vaultDao.softDelete(vault.id)

        val result = vaultDao.getActiveVault().first()
        assertNull(result)
    }

    @Test
    fun vaultItemDao_insertAndQueryByType() = runBlocking {
        val vaultId = UUID.randomUUID().toString()
        vaultDao.insert(VaultEntity(id = vaultId, name = "Item Vault"))

        val item = VaultItemEntity(
            id = UUID.randomUUID().toString(),
            vaultId = vaultId,
            type = "LOGIN",
            name = "Test Login",
            username = "user@test.com",
            password = "secret123"
        )
        vaultItemDao.insert(item)

        val items = vaultItemDao.getByType(vaultId, "LOGIN").first()
        assertTrue(items.isNotEmpty())
        assertEquals(item.name, items[0].name)
    }

    @Test
    fun vaultItemDao_searchItems() = runBlocking {
        val vaultId = UUID.randomUUID().toString()
        vaultDao.insert(VaultEntity(id = vaultId, name = "Search Vault"))

        val item = VaultItemEntity(
            id = UUID.randomUUID().toString(),
            vaultId = vaultId,
            type = "LOGIN",
            name = "GitHub Login",
            username = "dev@github.com",
            url = "https://github.com"
        )
        vaultItemDao.insert(item)

        val results = vaultItemDao.searchItems(vaultId, "github").first()
        assertTrue(results.isNotEmpty())
        assertEquals(item.id, results[0].id)
    }

    @Test
    fun vaultItemDao_softDeleteAndRestore() = runBlocking {
        val vaultId = UUID.randomUUID().toString()
        vaultDao.insert(VaultEntity(id = vaultId, name = "Restore Vault"))

        val item = VaultItemEntity(
            id = UUID.randomUUID().toString(),
            vaultId = vaultId,
            type = "NOTE",
            name = "Temporary Note"
        )
        vaultItemDao.insert(item)
        vaultItemDao.softDelete(item.id)

        val deleted = vaultItemDao.getDeleted(vaultId).first()
        assertTrue(deleted.isNotEmpty())

        vaultItemDao.restore(item.id)
        val active = vaultItemDao.getActiveItems(vaultId).first()
        assertTrue(active.isNotEmpty())
    }

    @Test
    fun vaultItemDao_favorites() = runBlocking {
        val vaultId = UUID.randomUUID().toString()
        vaultDao.insert(VaultEntity(id = vaultId, name = "Fav Vault"))

        val item = VaultItemEntity(
            id = UUID.randomUUID().toString(),
            vaultId = vaultId,
            type = "LOGIN",
            name = "Favorite Login",
            favorite = true
        )
        vaultItemDao.insert(item)

        val favorites = vaultItemDao.getFavorites(vaultId).first()
        assertTrue(favorites.isNotEmpty())
        assertTrue(favorites[0].favorite)
    }

    @Test
    fun vaultItemDao_permanentDelete() = runBlocking {
        val vaultId = UUID.randomUUID().toString()
        vaultDao.insert(VaultEntity(id = vaultId, name = "Perm Delete Vault"))

        val item = VaultItemEntity(
            id = UUID.randomUUID().toString(),
            vaultId = vaultId,
            type = "LOGIN",
            name = "Delete Me"
        )
        vaultItemDao.insert(item)
        vaultItemDao.softDelete(item.id)
        vaultItemDao.permanentDelete(item.id)

        val deleted = vaultItemDao.getDeleted(vaultId).first()
        assertTrue(deleted.isEmpty())
    }
}
