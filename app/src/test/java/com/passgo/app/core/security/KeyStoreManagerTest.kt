package com.passgo.app.core.security

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KeyStoreManagerTest {

    private lateinit var keyStoreManager: KeyStoreManager

    @BeforeEach
    fun setup() {
        keyStoreManager = KeyStoreManager()
    }

    @Test
    fun `clearCache exists as public API`() {
        assertDoesNotThrow { keyStoreManager::clearCache }
    }

    @Test
    fun `multiple clearCache calls are safe`() {
        assertDoesNotThrow { keyStoreManager.clearCache() }
        assertDoesNotThrow { keyStoreManager.clearCache() }
        assertDoesNotThrow { keyStoreManager.clearCache() }
    }

    @Test
    fun `clearCache is callable before any key operation`() {
        assertDoesNotThrow { keyStoreManager.clearCache() }
    }

    @Test
    fun `clearCache is available as public method`() {
        val methods = KeyStoreManager::class.java.methods
        val clearCacheMethod = methods.find { it.name == "clearCache" }
        assertNotNull(clearCacheMethod)
        assertDoesNotThrow { keyStoreManager.clearCache() }
    }
}
