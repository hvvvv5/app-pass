package com.passgo.app.feature.vault

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.model.Attachment
import com.passgo.app.data.repository.AttachmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PreviewUiState {
    data object Loading : PreviewUiState()
    data class ImageContent(val uri: Uri, val attachment: Attachment) : PreviewUiState()
    data class TextContent(val content: String, val attachment: Attachment) : PreviewUiState()
    data class ExternalContent(val uri: Uri, val mimeType: String, val attachment: Attachment) : PreviewUiState()
    data class Error(val message: String) : PreviewUiState()
}

@HiltViewModel
class AttachmentPreviewViewModel @Inject constructor(
    private val attachmentRepository: AttachmentRepository,
    private val logger: PassGoLogger,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var currentAttachmentId: String? = null
    private var loadJob: Job? = null

    private val _state = MutableStateFlow<PreviewUiState>(PreviewUiState.Loading)
    val state: StateFlow<PreviewUiState> = _state.asStateFlow()

    fun loadAttachment(attachmentId: String) {
        currentAttachmentId = attachmentId
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = PreviewUiState.Loading
            attachmentRepository.getAttachmentById(attachmentId).collect { attachment ->
                if (attachment != null) {
                    preparePreview(attachment)
                } else {
                    _state.value = PreviewUiState.Error("Attachment not found")
                }
            }
        }
    }

    private suspend fun preparePreview(attachment: Attachment) {
        when {
            attachment.mimeType.startsWith("image/") -> {
                when (val result = attachmentRepository.getAttachmentPreviewUri(attachment)) {
                    is AppResult.Success -> {
                        _state.value = PreviewUiState.ImageContent(result.data, attachment)
                    }
                    is AppResult.Error -> {
                        _state.value = PreviewUiState.Error("Failed to load image preview")
                    }
                }
            }
            attachment.mimeType.startsWith("text/") -> {
                when (val result = attachmentRepository.getAttachmentFile(attachment)) {
                    is AppResult.Success -> {
                        val content = result.data.decodeToString()
                        _state.value = PreviewUiState.TextContent(content, attachment)
                    }
                    is AppResult.Error -> {
                        _state.value = PreviewUiState.Error("Failed to load text preview")
                    }
                }
            }
            else -> {
                when (val result = attachmentRepository.getAttachmentPreviewUri(attachment)) {
                    is AppResult.Success -> {
                        _state.value = PreviewUiState.ExternalContent(
                            result.data, attachment.mimeType, attachment
                        )
                    }
                    is AppResult.Error -> {
                        _state.value = PreviewUiState.Error("Failed to load attachment")
                    }
                }
            }
        }
    }

    fun launchExternalViewer(
        context: Context,
        intentProvider: (Uri, String) -> Intent = { uri, mimeType ->
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    ) {
        val state = _state.value
        if (state !is PreviewUiState.ExternalContent) return
        val intent = intentProvider(state.uri, state.mimeType)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            _state.value = PreviewUiState.Error("No app available to open this file")
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentAttachmentId?.let { id ->
            File(context.cacheDir, "attachment_preview/$id").let { if (it.exists()) it.delete() }
        }
    }
}
