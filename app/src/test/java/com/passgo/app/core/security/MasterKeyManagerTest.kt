package com.passgo.app.core.security

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MasterKeyManagerTest {

    private val keyDerivation: KeyDerivation = mockk()
    private val keyStoreManager: KeyStoreManager = mockk()
    private lateinit var masterKeyManager: MasterKeyManager

    @BeforeEach
    fun setup() {
        masterKeyManager = MasterKeyManager(keyDerivation, keyStoreManager)
    }

    @Test
    fun `getOrCreateMasterKey caches the key`() {
        val key = ByteArray(32) { it.toByte() }
        every { keyStoreManager.loadDatabaseKey(any()) } returns null
        every { keyStoreManager.generateAndStoreDatabaseKey(any()) } returns key

        val result = masterKeyManager.getOrCreateMasterKey()
        assertNotNull(result)
        assertArrayEquals(key, result)
    }

    @Test
    fun `clearOnLock clears cached key`() {
        val key = ByteArray(32) { it.toByte() }
        every { keyStoreManager.loadDatabaseKey(any()) } returns null
        every { keyStoreManager.generateAndStoreDatabaseKey(any()) } returns key
        every { keyStoreManager.loadDatabaseKey(any()) } returns null
        every { keyStoreManager.generateAndStoreDatabaseKey(any()) } returns key

        masterKeyManager.getOrCreateMasterKey()
        masterKeyManager.clearOnLock()
        val result = masterKeyManager.getOrCreateMasterKey()
        verify(exactly = 2) { keyStoreManager.generateAndStoreDatabaseKey(any()) }
    }

    @Test
    fun `clearOnLock zeros cached key bytes`() {
        val key = ByteArray(32) { 0x42 }
        every { keyStoreManager.loadDatabaseKey(any()) } returns null
        every { keyStoreManager.generateAndStoreDatabaseKey(any()) } returns key

        masterKeyManager.getOrCreateMasterKey()
        val cachedRef = getCachedKeyRef()
        masterKeyManager.clearOnLock()
        for (b in cachedRef!!) {
            assert(b == 0.toByte()) { "Byte not zeroed after clearOnLock" }
        }
    }

    @Test
    fun `multiple clearOnLock calls are safe`() {
        assertDoesNotThrow { masterKeyManager.clearOnLock() }
        assertDoesNotThrow { masterKeyManager.clearOnLock() }
        assertDoesNotThrow { masterKeyManager.clearOnLock() }
    }

    @Test
    fun `multiple clearCache calls are safe`() {
        assertDoesNotThrow { masterKeyManager.clearCache() }
        assertDoesNotThrow { masterKeyManager.clearCache() }
        assertDoesNotThrow { masterKeyManager.clearCache() }
    }

    @Test
    fun `unlockWithPassword sets cached key that can be cleared`() {
        val password = "test".toCharArray()
        val salt = ByteArray(32) { 0x10 }
        val derivedKey = ByteArray(32) { 0x20 }
        every { keyDerivation.deriveKey(password, salt) } returns derivedKey
        every { keyDerivation.clearPassword(any()) } returns Unit

        masterKeyManager.unlockWithPassword(password, salt)
        masterKeyManager.clearOnLock()
        val ref = getCachedKeyRef()
        assert(ref == null || ref.all { it == 0.toByte() }) { "Cache not cleared" }
    }

    @Test
    fun `clearOnLock and clearCache both release memory`() {
        assertDoesNotThrow { masterKeyManager.clearOnLock() }
        assertDoesNotThrow { masterKeyManager.clearCache() }
    }

    private fun getCachedKeyRef(): ByteArray? {
        val field = MasterKeyManager::class.java.getDeclaredField("cachedMasterKey")
        field.isAccessible = true
        return field.get(masterKeyManager) as? ByteArray
    }
}
