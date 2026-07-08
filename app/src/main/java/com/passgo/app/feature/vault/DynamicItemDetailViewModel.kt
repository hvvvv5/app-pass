package com.passgo.app.feature.vault

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.security.ClipboardGuard
import com.passgo.app.core.model.CustomField
import com.passgo.app.core.model.FieldDefinition
import com.passgo.app.core.model.FieldId
import com.passgo.app.core.model.Folder
import com.passgo.app.core.model.Tag
import com.passgo.app.core.model.VaultItem
import com.passgo.app.data.repository.CustomFieldRepository
import com.passgo.app.data.repository.FolderRepository
import com.passgo.app.data.repository.TagRepository
import com.passgo.app.data.repository.VaultItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FieldDisplayValue(
    val fieldId: FieldId,
    val definition: FieldDefinition,
    val value: String
)

@HiltViewModel
class DynamicItemDetailViewModel @Inject constructor(
    private val vaultItemRepository: VaultItemRepository,
    private val customFieldRepository: CustomFieldRepository,
    private val tagRepository: TagRepository,
    private val folderRepository: FolderRepository,
    private val logger: PassGoLogger,
    private val clipboardGuard: ClipboardGuard,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val vaultId = "default"

    private val _item = MutableStateFlow<VaultItem?>(null)
    val item: StateFlow<VaultItem?> = _item.asStateFlow()

    private val _fieldValues = MutableStateFlow<List<FieldDisplayValue>>(emptyList())
    val fieldValues: StateFlow<List<FieldDisplayValue>> = _fieldValues.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    private val _copyFeedback = MutableStateFlow<String?>(null)
    val copyFeedback: StateFlow<String?> = _copyFeedback.asStateFlow()

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    val folders: StateFlow<List<Folder>> = folderRepository.getActiveFolders(vaultId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadItem(itemId: String) {
        viewModelScope.launch {
            val itemFlow = vaultItemRepository.getItemById(itemId)
            val customFieldsFlow = customFieldRepository.getFieldsForItem(itemId)

            combine(itemFlow, customFieldsFlow) { loaded, cfList ->
                loaded to cfList
            }.collect { (loaded, cfList) ->
                _item.value = loaded
                if (loaded != null) {
                    buildFieldValues(loaded, cfList)
                }
            }
        }
        viewModelScope.launch {
            tagRepository.getTagsForItem(itemId).collect { tagList ->
                _tags.value = tagList
            }
        }
    }

    private fun buildFieldValues(item: VaultItem, customFields: List<CustomField> = emptyList()) {
        val values = mutableListOf<FieldDisplayValue>()
        val standardMap = mapOf(
            FieldId.NAME to item.name,
            FieldId.USERNAME to item.username,
            FieldId.EMAIL_ADDRESS to item.email,
            FieldId.PASSWORD to item.password,
            FieldId.URL to item.url,
            FieldId.NOTES to item.notes
        )
        val customMap = customFields.associate { it.fieldId to it.value }

        item.category.fields.forEach { fieldId ->
            val value = when {
                fieldId in standardMap -> standardMap[fieldId] ?: ""
                fieldId in customMap -> customMap[fieldId] ?: ""
                else -> ""
            }
            if (value.isNotEmpty()) {
                values.add(
                    FieldDisplayValue(
                        fieldId = fieldId,
                        definition = FieldDefinition.fromId(fieldId),
                        value = value
                    )
                )
            }
        }
        _fieldValues.value = values
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun copyToClipboard(label: String, text: String) {
        clipboardGuard.copySensitiveText(text)
        _copyFeedback.value = "$label copied"
        logger.info("DynamicItemDetailViewModel", "Field copied  to clipboard")
    }

    fun clearCopyFeedback() {
        _copyFeedback.value = null
    }

    fun openWebsite(url: String) {
        val uri = if (url.startsWith("http://") || url.startsWith("https://")) {
            Uri.parse(url)
        } else {
            Uri.parse("https://$url")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    fun deleteItem(onDeleted: () -> Unit) {
        val currentId = _item.value?.id ?: return
        viewModelScope.launch {
            when (vaultItemRepository.softDelete(currentId)) {
                is AppResult.Success -> {
                    logger.info("DynamicItemDetailViewModel", "Deleted item: $currentId")
                    onDeleted()
                }
                is AppResult.Error -> logger.error("DynamicItemDetailViewModel", "Delete failed")
            }
        }
    }

    fun archiveItem(onArchived: () -> Unit) {
        val currentId = _item.value?.id ?: return
        viewModelScope.launch {
            when (vaultItemRepository.archive(currentId)) {
                is AppResult.Success -> {
                    logger.info("DynamicItemDetailViewModel", "Archived item: $currentId")
                    onArchived()
                }
                is AppResult.Error -> logger.error("DynamicItemDetailViewModel", "Archive failed")
            }
        }
    }

    fun moveItem(folderId: String?) {
        val currentId = _item.value?.id ?: return
        viewModelScope.launch {
            when (vaultItemRepository.moveItem(currentId, folderId)) {
                is AppResult.Success -> logger.info("DynamicItemDetailViewModel", "Moved item $currentId")
                is AppResult.Error -> logger.error("DynamicItemDetailViewModel", "Move failed")
            }
        }
    }
}
