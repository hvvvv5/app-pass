package com.passgo.app.data.repository

import android.net.Uri
import com.passgo.app.core.database.dao.AttachmentDao
import com.passgo.app.core.error.AppException
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.model.Attachment
import com.passgo.app.core.security.AttachmentManager
import com.passgo.app.data.mapper.toDomain
import com.passgo.app.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentRepositoryImpl @Inject constructor(
    private val attachmentDao: AttachmentDao,
    private val attachmentManager: AttachmentManager
) : AttachmentRepository {

    override fun getAttachmentsForItem(itemId: String): Flow<List<Attachment>> =
        attachmentDao.getAttachmentsForItem(itemId).map { list -> list.map { it.toDomain() } }

    override fun getAttachmentById(id: String): Flow<Attachment?> =
        attachmentDao.getAttachmentById(id).map { it?.toDomain() }

    override suspend fun insert(attachment: Attachment): AppResult<Unit> = runCatching {
        attachmentDao.insert(attachment.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun update(attachment: Attachment): AppResult<Unit> = runCatching {
        attachmentDao.update(attachment.toEntity())
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun softDelete(id: String): AppResult<Unit> = runCatching {
        attachmentDao.softDelete(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )

    override suspend fun addAttachment(
        sourceUri: Uri,
        itemId: String,
        name: String,
        mimeType: String
    ): AppResult<Attachment> {
        val attachmentId = UUID.randomUUID().toString()
        return when (val result = attachmentManager.encryptAndStore(sourceUri, attachmentId)) {
            is AppResult.Error -> AppResult.Error(result.exception)
            is AppResult.Success -> {
                val now = System.currentTimeMillis()
                val attachment = Attachment(
                    id = attachmentId,
                    itemId = itemId,
                    name = name,
                    mimeType = mimeType,
                    encryptedFileUri = result.data.encryptedFileUri,
                    encryptionIv = result.data.encryptionIv,
                    contentHash = result.data.contentHash,
                    sizeBytes = result.data.sizeBytes,
                    createdAt = now,
                    updatedAt = now
                )
                when (val insertResult = insert(attachment)) {
                    is AppResult.Error -> {
                        attachmentManager.deleteFile(attachmentId)
                        insertResult
                    }
                    is AppResult.Success -> AppResult.Success(attachment)
                }
            }
        }
    }

    override suspend fun getAttachmentFile(attachment: Attachment): AppResult<ByteArray> {
        return attachmentManager.decryptToBytes(attachment.encryptedFileUri, attachment.encryptionIv)
    }

    override suspend fun getAttachmentPreviewUri(attachment: Attachment): AppResult<Uri> {
        return attachmentManager.decryptToCacheFile(
            attachment.encryptedFileUri,
            attachment.encryptionIv,
            attachment.mimeType
        )
    }

    override suspend fun deleteAttachmentPermanently(id: String): AppResult<Unit> = runCatching {
        attachmentDao.permanentDelete(id)
        attachmentManager.deleteFile(id)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
    )
}
