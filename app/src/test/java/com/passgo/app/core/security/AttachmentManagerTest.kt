package com.passgo.app.core.security

import android.content.ContentResolver
import android.content.Context
import com.passgo.app.core.error.AppResult
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class AttachmentManagerTest {

    @TempDir
    lateinit var tempDir: File

    private fun sha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data).joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `encryptAndStore stores encrypted file and returns result`() = runBlocking {
        val keyStoreManager = mockk<KeyStoreManager>()
        val context = mockk<Context>(relaxed = true)
        val contentResolver = mockk<ContentResolver>(relaxed = true)
        val plaintext = "secret data".toByteArray()
        val expectedIv = "iv_bytes".toByteArray()
        val expectedCiphertext = "encrypted_bytes".toByteArray()

        every { context.contentResolver } returns contentResolver
        every { context.filesDir } returns tempDir

        val sourceUri = mockk<android.net.Uri>(relaxed = true)
        every { contentResolver.openInputStream(sourceUri) } returns plaintext.inputStream()

        every { keyStoreManager.encrypt(plaintext) } returns KeyStoreManager.EncryptedData(expectedCiphertext, expectedIv)

        val manager = AttachmentManager(keyStoreManager, context)
        val result = manager.encryptAndStore(sourceUri, "att_1")

        val success = result as AppResult.Success
        assertEquals("att_1", success.data.encryptedFileUri)
        assertEquals(plaintext.size.toLong(), success.data.sizeBytes)
        assertArrayEquals(expectedIv, success.data.encryptionIv)
        assertEquals(sha256(plaintext), success.data.contentHash)

        val storedFile = File(tempDir, "attachments/att_1")
        assertTrue(storedFile.exists())
        assertArrayEquals(expectedCiphertext, storedFile.readBytes())
    }

    @Test
    fun `encryptAndStore returns error when file exceeds max size`() = runBlocking {
        val keyStoreManager = mockk<KeyStoreManager>()
        val context = mockk<Context>(relaxed = true)
        val contentResolver = mockk<ContentResolver>(relaxed = true)

        every { context.contentResolver } returns contentResolver
        every { context.filesDir } returns tempDir

        val largeData = ByteArray((20 * 1024 * 1024) + 1)
        val sourceUri = mockk<android.net.Uri>(relaxed = true)
        every { contentResolver.openInputStream(sourceUri) } returns largeData.inputStream()

        val manager = AttachmentManager(keyStoreManager, context)
        val result = manager.encryptAndStore(sourceUri, "att_large")

        assertTrue(result is AppResult.Error)
    }

    @Test
    fun `decryptToBytes reads and decrypts stored file`() = runBlocking {
        val keyStoreManager = mockk<KeyStoreManager>()
        val context = mockk<Context>(relaxed = true)
        val contentResolver = mockk<ContentResolver>(relaxed = true)
        val plaintext = "restored data".toByteArray()
        val iv = "test_iv_12bytes".toByteArray()
        val ciphertext = "encrypted_output".toByteArray()

        every { context.contentResolver } returns contentResolver
        every { context.filesDir } returns tempDir

        val sourceUri = mockk<android.net.Uri>(relaxed = true)
        every { contentResolver.openInputStream(sourceUri) } returns plaintext.inputStream()
        every { keyStoreManager.encrypt(any()) } returns KeyStoreManager.EncryptedData(ciphertext, iv)
        every { keyStoreManager.decrypt(any()) } returns plaintext

        val manager = AttachmentManager(keyStoreManager, context)
        manager.encryptAndStore(sourceUri, "att_decrypt")

        val result = manager.decryptToBytes("att_decrypt", iv)
        val success = result as AppResult.Success
        assertArrayEquals(plaintext, success.data)
    }

    @Test
    fun `decryptToBytes returns error for missing file`() = runBlocking {
        val keyStoreManager = mockk<KeyStoreManager>()
        val context = mockk<Context>(relaxed = true)
        every { context.filesDir } returns tempDir

        val manager = AttachmentManager(keyStoreManager, context)
        val result = manager.decryptToBytes("nonexistent", byteArrayOf())
        assertTrue(result is AppResult.Error)
    }

    @Test
    fun `deleteFile removes encrypted and cache files`() = runBlocking {
        val keyStoreManager = mockk<KeyStoreManager>()
        val context = mockk<Context>(relaxed = true)
        val contentResolver = mockk<ContentResolver>(relaxed = true)
        val plaintext = "delete_test".toByteArray()

        every { context.contentResolver } returns contentResolver
        every { context.filesDir } returns tempDir
        every { context.cacheDir } returns File(tempDir, "cache").also { it.mkdirs() }

        val sourceUri = mockk<android.net.Uri>(relaxed = true)
        every { contentResolver.openInputStream(sourceUri) } returns plaintext.inputStream()
        every { keyStoreManager.encrypt(any()) } returns KeyStoreManager.EncryptedData(plaintext, byteArrayOf())

        val manager = AttachmentManager(keyStoreManager, context)
        manager.encryptAndStore(sourceUri, "att_del")

        val encryptedFile = File(tempDir, "attachments/att_del")
        assertTrue(encryptedFile.exists())

        val result = manager.deleteFile("att_del")
        assertTrue(result is AppResult.Success)
        assertFalse(encryptedFile.exists())
    }

    @Test
    fun `deleteFile succeeds when file does not exist`() = runBlocking {
        val keyStoreManager = mockk<KeyStoreManager>()
        val context = mockk<Context>(relaxed = true)
        every { context.filesDir } returns tempDir
        every { context.cacheDir } returns File(tempDir, "cache")

        val manager = AttachmentManager(keyStoreManager, context)
        val result = manager.deleteFile("never_created")
        assertTrue(result is AppResult.Success)
    }
}
