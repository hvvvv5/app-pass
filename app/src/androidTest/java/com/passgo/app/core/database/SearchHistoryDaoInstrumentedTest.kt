package com.passgo.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.passgo.app.core.database.dao.SearchHistoryDao
import com.passgo.app.core.database.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.SecureRandom
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SearchHistoryDaoInstrumentedTest {

    private lateinit var database: PassGoDatabase
    private lateinit var searchHistoryDao: SearchHistoryDao
    private val vaultId = "test_vault"

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val passphrase = ByteArray(32).apply { SecureRandom().nextBytes(this) }
        val factory = SupportOpenHelperFactory(passphrase)

        database = Room.inMemoryDatabaseBuilder(context, PassGoDatabase::class.java)
            .openHelperFactory(factory)
            .build()

        searchHistoryDao = database.searchHistoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveRecentSearches() = runBlocking {
        val search = SearchHistoryEntity(
            id = UUID.randomUUID().toString(),
            vaultId = vaultId,
            query = "github",
            createdAt = System.currentTimeMillis()
        )
        searchHistoryDao.insert(search)

        val results = searchHistoryDao.getRecentSearches(vaultId).first()
        assertEquals(1, results.size)
        assertEquals("github", results[0].query)
    }

    @Test
    fun recentSearchesReturnsMostRecentFirst() = runBlocking {
        val search1 = SearchHistoryEntity(
            id = UUID.randomUUID().toString(),
            vaultId = vaultId,
            query = "old search",
            createdAt = 1000
        )
        val search2 = SearchHistoryEntity(
            id = UUID.randomUUID().toString(),
            vaultId = vaultId,
            query = "recent search",
            createdAt = 2000
        )
        searchHistoryDao.insert(search1)
        searchHistoryDao.insert(search2)

        val results = searchHistoryDao.getRecentSearches(vaultId).first()
        assertEquals(2, results.size)
        assertEquals("recent search", results[0].query)
        assertEquals("old search", results[1].query)
    }

    @Test
    fun recentSearchesRespectsLimit() = runBlocking {
        for (i in 1..5) {
            searchHistoryDao.insert(
                SearchHistoryEntity(
                    id = UUID.randomUUID().toString(),
                    vaultId = vaultId,
                    query = "search $i",
                    createdAt = i.toLong()
                )
            )
        }

        val results = searchHistoryDao.getRecentSearches(vaultId, 3).first()
        assertEquals(3, results.size)
    }

    @Test
    fun deleteByIdRemovesSearch() = runBlocking {
        val id = UUID.randomUUID().toString()
        searchHistoryDao.insert(
            SearchHistoryEntity(
                id = id,
                vaultId = vaultId,
                query = "delete me",
                createdAt = System.currentTimeMillis()
            )
        )

        searchHistoryDao.deleteById(id)

        val results = searchHistoryDao.getRecentSearches(vaultId).first()
        assertTrue(results.isEmpty())
    }

    @Test
    fun deleteAllForVaultClearsAll() = runBlocking {
        searchHistoryDao.insert(
            SearchHistoryEntity(
                id = UUID.randomUUID().toString(),
                vaultId = vaultId,
                query = "item1",
                createdAt = System.currentTimeMillis()
            )
        )
        searchHistoryDao.insert(
            SearchHistoryEntity(
                id = UUID.randomUUID().toString(),
                vaultId = vaultId,
                query = "item2",
                createdAt = System.currentTimeMillis()
            )
        )

        searchHistoryDao.deleteAllForVault(vaultId)

        val results = searchHistoryDao.getRecentSearches(vaultId).first()
        assertTrue(results.isEmpty())
    }

    @Test
    fun differentVaultsAreIndependent() = runBlocking {
        searchHistoryDao.insert(
            SearchHistoryEntity(
                id = UUID.randomUUID().toString(),
                vaultId = vaultId,
                query = "vault1 query",
                createdAt = System.currentTimeMillis()
            )
        )
        searchHistoryDao.insert(
            SearchHistoryEntity(
                id = UUID.randomUUID().toString(),
                vaultId = "other_vault",
                query = "vault2 query",
                createdAt = System.currentTimeMillis()
            )
        )

        val results = searchHistoryDao.getRecentSearches(vaultId).first()
        assertEquals(1, results.size)
        assertEquals("vault1 query", results[0].query)
    }

    @Test
    fun getSearchCountReturnsCorrectCount() = runBlocking {
        assertEquals(0, searchHistoryDao.getSearchCount(vaultId))

        searchHistoryDao.insert(
            SearchHistoryEntity(
                id = UUID.randomUUID().toString(),
                vaultId = vaultId,
                query = "count test",
                createdAt = System.currentTimeMillis()
            )
        )

        assertEquals(1, searchHistoryDao.getSearchCount(vaultId))
    }

    @Test
    fun deleteOldestKeepsOnlySpecifiedCount() = runBlocking {
        for (i in 1..10) {
            searchHistoryDao.insert(
                SearchHistoryEntity(
                    id = UUID.randomUUID().toString(),
                    vaultId = vaultId,
                    query = "search $i",
                    createdAt = i.toLong()
                )
            )
        }

        searchHistoryDao.deleteOldest(vaultId, 5)

        assertEquals(5, searchHistoryDao.getSearchCount(vaultId))
    }

    @Test
    fun getExistingQueryIdReturnsIdForDuplicate() = runBlocking {
        val id = UUID.randomUUID().toString()
        searchHistoryDao.insert(
            SearchHistoryEntity(
                id = id,
                vaultId = vaultId,
                query = "duplicate query",
                createdAt = System.currentTimeMillis()
            )
        )

        val foundId = searchHistoryDao.getExistingQueryId(vaultId, "duplicate query")
        assertNotNull(foundId)
        assertEquals(id, foundId)
    }

    @Test
    fun getExistingQueryIdReturnsNullForNewQuery() = runBlocking {
        val result = searchHistoryDao.getExistingQueryId(vaultId, "nonexistent")
        assertNull(result)
    }

    @Test
    fun emptyVaultReturnsEmptyList() = runBlocking {
        val results = searchHistoryDao.getRecentSearches(vaultId).first()
        assertTrue(results.isEmpty())
    }

    @Test
    fun deleteOldestWithNoEntriesDoesNotCrash() = runBlocking {
        searchHistoryDao.deleteOldest(vaultId, 10)
        assertEquals(0, searchHistoryDao.getSearchCount(vaultId))
    }
}
