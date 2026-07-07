package com.passgo.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.passgo.app.core.database.dao.VaultItemDao
import com.passgo.app.core.database.entity.VaultEntity
import com.passgo.app.core.database.entity.VaultItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.SecureRandom
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DaoFtsInstrumentedTest {

    private lateinit var database: PassGoDatabase
    private lateinit var vaultItemDao: VaultItemDao
    private val vaultId = UUID.randomUUID().toString()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val passphrase = ByteArray(32).apply { SecureRandom().nextBytes(this) }
        val factory = SupportOpenHelperFactory(passphrase)

        database = Room.inMemoryDatabaseBuilder(context, PassGoDatabase::class.java)
            .openHelperFactory(factory)
            .build()

        vaultItemDao = database.vaultItemDao()

        runBlocking {
            database.vaultDao().insert(VaultEntity(id = vaultId, name = "FTS Test Vault"))
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun insertItem(
        id: String = UUID.randomUUID().toString(),
        name: String = "Test Item",
        username: String = "",
        email: String = "",
        url: String = "",
        notes: String = "",
        type: String = "LOGIN",
        favorite: Boolean = false
    ) = runBlocking {
        vaultItemDao.insert(
            VaultItemEntity(
                id = id,
                vaultId = vaultId,
                type = type,
                name = name,
                username = username,
                email = email,
                url = url,
                notes = notes,
                favorite = favorite
            )
        )
    }

    @Test
    fun searchItemsFts_returnsMatchingItems() = runBlocking {
        insertItem(id = "fts1", name = "GitHub Account", username = "dev", email = "dev@github.com")

        val results = vaultItemDao.searchItemsFts(vaultId, "github").first()
        assertTrue("FTS should find 'GitHub Account' by name", results.isNotEmpty())
        assertEquals("fts1", results[0].id)
    }

    @Test
    fun searchItemsFts_matchesUsername() = runBlocking {
        insertItem(id = "fts2", name = "My Login", username = "john_doe", email = "john@test.com")

        val results = vaultItemDao.searchItemsFts(vaultId, "john_doe").first()
        assertTrue("FTS should find by username", results.isNotEmpty())
    }

    @Test
    fun searchItemsFts_matchesEmail() = runBlocking {
        insertItem(id = "fts3", name = "Email Login", email = "test@example.com")

        val results = vaultItemDao.searchItemsFts(vaultId, "example.com").first()
        assertTrue("FTS should find by email", results.isNotEmpty())
    }

    @Test
    fun searchItemsFts_matchesUrl() = runBlocking {
        insertItem(id = "fts4", name = "Website", url = "https://example.com/login")

        val results = vaultItemDao.searchItemsFts(vaultId, "example.com").first()
        assertTrue("FTS should find by URL", results.isNotEmpty())
    }

    @Test
    fun searchItemsFts_matchesNotes() = runBlocking {
        insertItem(id = "fts5", name = "Note Item", notes = "This is a secret note")

        val results = vaultItemDao.searchItemsFts(vaultId, "secret").first()
        assertTrue("FTS should find by notes", results.isNotEmpty())
    }

    @Test
    fun searchItemsFts_prefixMatch() = runBlocking {
        insertItem(id = "fts6", name = "StackOverflow Login")

        val results = vaultItemDao.searchItemsFts(vaultId, "stack*").first()
        assertTrue("FTS should support prefix matching", results.isNotEmpty())
    }

    @Test
    fun searchItemsFts_excludesDeletedItems() = runBlocking {
        insertItem(id = "fts7", name = "Deleted Item")
        vaultItemDao.softDelete("fts7")

        val results = vaultItemDao.searchItemsFts(vaultId, "deleted").first()
        assertTrue("FTS should exclude soft-deleted items", results.isEmpty())
    }

    @Test
    fun searchItemsFts_excludesArchivedItems() = runBlocking {
        insertItem(id = "fts8", name = "Archived Item")
        vaultItemDao.archive("fts8")

        val results = vaultItemDao.searchItemsFts(vaultId, "archived").first()
        assertTrue("FTS should exclude archived items", results.isEmpty())
    }

    @Test
    fun searchItemsFts_emptyQuery_returnsNothing() = runBlocking {
        insertItem(id = "fts9", name = "Some Item")

        val results = vaultItemDao.searchItemsFts(vaultId, "").first()
        assertTrue("Empty FTS query should return no results", results.isEmpty())
    }

    @Test
    fun searchItemsFts_noMatch_returnsEmpty() = runBlocking {
        insertItem(id = "fts10", name = "Unique Name")

        val results = vaultItemDao.searchItemsFts(vaultId, "nonexistent").first()
        assertTrue("FTS should return empty for no match", results.isEmpty())
    }

    @Test
    fun searchByTypeFts_filtersByType() = runBlocking {
        insertItem(id = "fts11", name = "Login Item", type = "LOGIN")
        insertItem(id = "fts12", name = "Note Item", type = "NOTE")

        val results = vaultItemDao.searchByTypeFts(vaultId, "NOTE", "item").first()
        assertTrue(results.isNotEmpty())
        assertEquals("fts12", results[0].id)
    }

    @Test
    fun searchFavoritesFts_filtersByFavorite() = runBlocking {
        insertItem(id = "fts13", name = "Favorite Item", favorite = true)
        insertItem(id = "fts14", name = "Regular Item", favorite = false)

        val results = vaultItemDao.searchFavoritesFts(vaultId, "item").first()
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.favorite })
    }

    @Test
    fun searchItemsFts_likeFallbackStillWorks() = runBlocking {
        insertItem(id = "fts15", name = "LIKE Test Item", username = "likeuser")

        val ftsResults = vaultItemDao.searchItemsFts(vaultId, "likeuser").first()
        val likeResults = vaultItemDao.searchItems(vaultId, "likeuser").first()

        assertTrue("FTS should find the item", ftsResults.isNotEmpty())
        assertTrue("LIKE should also find the item", likeResults.isNotEmpty())
    }

    @Test
    fun searchItemsFts_multipleWords() = runBlocking {
        insertItem(id = "fts16", name = "Google", notes = "Search engine and cloud provider")

        val results = vaultItemDao.searchItemsFts(vaultId, "engine").first()
        assertTrue("FTS should match second word in notes", results.isNotEmpty())
    }
}
