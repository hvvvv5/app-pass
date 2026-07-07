package com.passgo.app.core.model

data class Attachment(
    val id: String,
    val itemId: String,
    val name: String,
    val mimeType: String = "application/octet-stream",
    val encryptedFileUri: String = "",
    val encryptionIv: ByteArray = byteArrayOf(),
    val contentHash: String = "",
    val sizeBytes: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val syncVersion: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
