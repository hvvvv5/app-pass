package com.passgo.app.feature.vault

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passgo.app.core.model.Attachment

@Composable
fun AttachmentSection(
    itemId: String,
    onPreview: (String) -> Unit = {},
    viewModel: AttachmentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val attachments by viewModel.attachments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAdding by viewModel.isAdding.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(itemId) {
        viewModel.loadAttachments(itemId)
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var attachmentToDelete by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val name = if (nameIndex >= 0) c.getString(nameIndex) ?: "Unknown" else "Unknown"
                    val mimeType = context.contentResolver.getType(it) ?: "application/octet-stream"
                    viewModel.addAttachment(it, name, mimeType)
                }
            } ?: run {
                val mimeType = context.contentResolver.getType(it) ?: "application/octet-stream"
                viewModel.addAttachment(it, "Attachment", mimeType)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            "Attachments",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (isLoading && attachments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        } else if (attachments.isEmpty()) {
            Text(
                "No attachments",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            attachments.forEach { attachment ->
                AttachmentRow(
                    attachment = attachment,
                    onPreview = { onPreview(attachment.id) },
                    onDelete = {
                        attachmentToDelete = attachment.id
                        showDeleteDialog = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
            enabled = !isAdding,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isAdding) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(Icons.Default.Add, contentDescription = "Add attachment", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text("Add Attachment")
        }
    }

    if (showDeleteDialog && attachmentToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                attachmentToDelete = null
            },
            title = { Text("Delete Attachment") },
            text = { Text("Delete this attachment permanently? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    attachmentToDelete?.let { viewModel.deleteAttachment(it) }
                    showDeleteDialog = false
                    attachmentToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    attachmentToDelete = null
                }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AttachmentRow(
    attachment: Attachment,
    onPreview: () -> Unit = {},
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = mimeTypeIcon(attachment.mimeType),
            contentDescription = attachment.mimeType,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = formatFileSize(attachment.sizeBytes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onPreview) {
            Icon(
                Icons.Default.Visibility,
                contentDescription = "Preview",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun mimeTypeIcon(mimeType: String): ImageVector {
    return when {
        mimeType.startsWith("image/") -> Icons.Default.Image
        else -> Icons.Default.Description
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}
