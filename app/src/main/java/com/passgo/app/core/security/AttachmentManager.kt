package com.passgo.app.core.security

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.passgo.app.core.error.AppException
import com.passgo.app.core.error.AppResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AttachmentManager @Inject constructor(
    private val keyStoreManager: KeyStoreManager,
    @ApplicationContext private val context: Context
) {

    private val storageDir: File
        get() = File(context.filesDir, ATTACHMENTS_DIR)

    private val previewCacheDir: File
        get() = File(context.cacheDir, PREVIEW_DIR)

    suspend fun encryptAndStore(
        sourceUri: Uri,
        attachmentId: String
    ): AppResult<EncryptedFileResult> = withContext(Dispatchers.IO) {
        runCatching {
            val plaintext = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                val bytes = input.readBytes()
                if (bytes.size > MAX_SIZE_BYTES) {
                    throw AppException.UnknownException(
                        "File exceeds maximum size of ${MAX_SIZE_BYTES / (1024 * 1024)} MB"
                    )
                }
                bytes
            } ?: throw AppException.UnknownException("Unable to read source file")

            val contentHash = computeSha256(plaintext)
            val encrypted = keyStoreManager.encrypt(plaintext)
            val targetFile = File(storageDir, attachmentId)

            targetFile.parentFile?.mkdirs()
            FileOutputStream(targetFile).use { it.write(encrypted.data) }

            EncryptedFileResult(
                encryptedFileUri = attachmentId,
                sizeBytes = plaintext.size.toLong(),
                encryptionIv = encrypted.iv,
                contentHash = contentHash
            )
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
        )
    }

    suspend fun decryptToBytes(
        attachmentId: String,
        encryptionIv: ByteArray
    ): AppResult<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            val encryptedFile = File(storageDir, attachmentId)
            if (!encryptedFile.exists()) {
                throw AppException.UnknownException("Encrypted file not found: $attachmentId")
            }
            val ciphertext = FileInputStream(encryptedFile).use { it.readBytes() }
            keyStoreManager.decrypt(KeyStoreManager.EncryptedData(ciphertext, encryptionIv))
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
        )
    }

    suspend fun decryptToCacheFile(
        attachmentId: String,
        encryptionIv: ByteArray,
        mimeType: String
    ): AppResult<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            when (val result = decryptToBytes(attachmentId, encryptionIv)) {
                is AppResult.Success -> {
                    val cacheFile = File(previewCacheDir, attachmentId)
                    cacheFile.parentFile?.mkdirs()
                    cacheFile.outputStream().use { it.write(result.data) }
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        cacheFile
                    )
                }
                is AppResult.Error -> throw result.exception
            }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
        )
    }

    suspend fun deleteFile(attachmentId: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val encryptedFile = File(storageDir, attachmentId)
            if (encryptedFile.exists() && !encryptedFile.delete()) {
                throw AppException.UnknownException("Failed to delete file: $attachmentId")
            }
            val cacheFile = File(previewCacheDir, attachmentId)
            if (cacheFile.exists()) cacheFile.delete()
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
        )
    }

    private fun computeSha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data).joinToString("") { "%02x".format(it) }
    }

    data class EncryptedFileResult(
        val encryptedFileUri: String,
        val sizeBytes: Long,
        val encryptionIv: ByteArray,
        val contentHash: String
    )

    companion object {
        private const val ATTACHMENTS_DIR = "attachments"
        private const val PREVIEW_DIR = "attachment_preview"
        private const val MAX_SIZE_BYTES = 20L * 1024 * 1024
    }
}
