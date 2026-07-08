package com.passgo.app.feature.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.model.CustomField
import com.passgo.app.core.model.FieldDefinition
import com.passgo.app.core.model.FieldId
import com.passgo.app.core.model.FieldValidationResult
import com.passgo.app.core.model.Folder
import com.passgo.app.core.model.Tag
import com.passgo.app.core.model.VaultItem
import com.passgo.app.core.model.VaultItemCategory
import com.passgo.app.core.security.PasswordGenerator
import com.passgo.app.core.security.PasswordValidator
import com.passgo.app.data.repository.CustomFieldRepository
import com.passgo.app.data.repository.FolderRepository
import com.passgo.app.data.repository.TagRepository
import com.passgo.app.data.repository.VaultItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class FieldValue(
    val value: String = "",
    val error: String? = null
)

@HiltViewModel
class DynamicFormViewModel @Inject constructor(
    private val vaultItemRepository: VaultItemRepository,
    private val customFieldRepository: CustomFieldRepository,
    private val folderRepository: FolderRepository,
    private val tagRepository: TagRepository,
    private val passwordGenerator: PasswordGenerator,
    private val passwordValidator: PasswordValidator,
    private val logger: PassGoLogger
) : ViewModel() {

    private val vaultId = "default"
    private var editItemId: String? = null

    private val _category = MutableStateFlow(VaultItemCategory.OTHER)
    val category: StateFlow<VaultItemCategory> = _category.asStateFlow()

    private val _fieldValues = MutableStateFlow<Map<FieldId, FieldValue>>(emptyMap())
    val fieldValues: StateFlow<Map<FieldId, FieldValue>> = _fieldValues.asStateFlow()

    private val _favorite = MutableStateFlow(false)
    val favorite: StateFlow<Boolean> = _favorite.asStateFlow()

    private val _selectedFolderId = MutableStateFlow<String?>(null)
    val selectedFolderId: StateFlow<String?> = _selectedFolderId.asStateFlow()

    private val _selectedTagIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedTagIds: StateFlow<Set<String>> = _selectedTagIds.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveComplete = MutableStateFlow(false)
    val saveComplete: StateFlow<Boolean> = _saveComplete.asStateFlow()

    private val _passwordStrength = MutableStateFlow(PasswordValidator.PasswordStrength.VERY_WEAK)
    val passwordStrength: StateFlow<PasswordValidator.PasswordStrength> = _passwordStrength.asStateFlow()

    val folders: StateFlow<List<Folder>> = folderRepository.getActiveFolders(vaultId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tags: StateFlow<List<Tag>> = tagRepository.getActiveTags(vaultId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun initialize(category: VaultItemCategory, itemId: String? = null) {
        _category.value = category
        editItemId = itemId

        val initialValues = mutableMapOf<FieldId, FieldValue>()
        category.fields.forEach { fieldId ->
            initialValues[fieldId] = FieldValue()
        }
        _fieldValues.value = initialValues

        if (itemId != null) {
            loadItem(itemId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadItem(itemId: String) {
        viewModelScope.launch {
            val itemFlow = vaultItemRepository.getItemById(itemId)
            val customFieldsFlow = customFieldRepository.getFieldsForItem(itemId)
            val tagFlow = tagRepository.getTagsForItem(itemId)

            combine(itemFlow, customFieldsFlow) { loaded, cfList ->
                loaded to cfList
            }.collect { (loaded, cfList) ->
                if (loaded != null) {
                    _category.value = loaded.category
                    _favorite.value = loaded.favorite
                    _selectedFolderId.value = loaded.folderId

                    val updated = _fieldValues.value.toMutableMap()
                    setStandardField(updated, FieldId.NAME, loaded.name)
                    setStandardField(updated, FieldId.USERNAME, loaded.username)
                    setStandardField(updated, FieldId.EMAIL_ADDRESS, loaded.email)
                    setStandardField(updated, FieldId.PASSWORD, loaded.password)
                    setStandardField(updated, FieldId.URL, loaded.url)
                    setStandardField(updated, FieldId.NOTES, loaded.notes)
                    cfList.forEach { cf ->
                        if (cf.fieldId in _category.value.fields) {
                            updated[cf.fieldId] = FieldValue(value = cf.value)
                        }
                    }
                    _fieldValues.value = updated
                }
            }
        }
        viewModelScope.launch {
            tagRepository.getTagsForItem(itemId).collect { tagList ->
                _selectedTagIds.value = tagList.map { it.id }.toSet()
            }
        }
    }

    private fun setStandardField(map: MutableMap<FieldId, FieldValue>, fieldId: FieldId, value: String) {
        if (fieldId in map) {
            map[fieldId] = FieldValue(value = value)
        }
    }

    fun setFieldValue(fieldId: FieldId, value: String) {
        val updated = _fieldValues.value.toMutableMap()
        updated[fieldId] = FieldValue(value = value, error = null)
        _fieldValues.value = updated

        if (fieldId == FieldId.PASSWORD) {
            _passwordStrength.value = passwordValidator.calculateStrength(value.toCharArray())
        }
    }

    fun setFavorite(value: Boolean) { _favorite.value = value }
    fun setFolder(folderId: String?) { _selectedFolderId.value = folderId }

    fun toggleTag(tagId: String) {
        val current = _selectedTagIds.value
        _selectedTagIds.value = if (tagId in current) current - tagId else current + tagId
    }

    fun generatePassword() {
        val generated = passwordGenerator.generate(
            PasswordGenerator.GeneratorOptions(
                length = 20,
                includeUppercase = true,
                includeLowercase = true,
                includeDigits = true,
                includeSymbols = true,
                excludeAmbiguous = true
            )
        )
        setFieldValue(FieldId.PASSWORD, String(generated))
        passwordGenerator.wipePassword(generated)
        logger.info("DynamicFormViewModel", "Password generated")
    }

    fun save() {
        val currentValues = _fieldValues.value
        var hasError = false
        val validated = currentValues.toMutableMap()

        _category.value.fields.forEach { fieldId ->
            val fv = currentValues[fieldId] ?: return@forEach
            val definition = FieldDefinition.fromId(fieldId)
            val result = definition.validate(fv.value)
            if (result is FieldValidationResult.Invalid) {
                validated[fieldId] = fv.copy(error = result.message)
                hasError = true
            }
        }

        _fieldValues.value = validated
        if (hasError) return

        _isSaving.value = true
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val cat = _category.value
            val values = _fieldValues.value

            val itemId = editItemId ?: UUID.randomUUID().toString()
            val item = VaultItem(
                id = itemId,
                vaultId = vaultId,
                folderId = _selectedFolderId.value,
                category = cat,
                name = values[FieldId.NAME]?.value?.trim() ?: "",
                username = values[FieldId.USERNAME]?.value?.trim() ?: "",
                email = values[FieldId.EMAIL_ADDRESS]?.value?.trim() ?: "",
                password = values[FieldId.PASSWORD]?.value ?: "",
                url = values[FieldId.URL]?.value?.trim() ?: "",
                notes = values[FieldId.NOTES]?.value?.trim() ?: "",
                favorite = _favorite.value,
                createdAt = if (editItemId != null) 0L else now,
                updatedAt = now
            )

            val itemResult = if (editItemId != null) {
                vaultItemRepository.update(item)
            } else {
                vaultItemRepository.insert(item)
            }

            when (itemResult) {
                is AppResult.Success -> {
                    val customFields = cat.fields.filter { it !in standardFieldIds }.mapNotNull { fieldId ->
                        values[fieldId]?.let { fv ->
                            if (fv.value.isNotBlank()) {
                                CustomField(
                                    id = UUID.randomUUID().toString(),
                                    itemId = itemId,
                                    fieldId = fieldId,
                                    value = fv.value,
                                    sortOrder = cat.fields.indexOf(fieldId)
                                )
                            } else null
                        }
                    }
                    customFieldRepository.deleteAllForItem(itemId)
                    if (customFields.isNotEmpty()) {
                        customFieldRepository.saveFields(customFields)
                    }
                    tagRepository.setItemTags(itemId, _selectedTagIds.value.toList())
                    logger.info("DynamicFormViewModel", "Saved item: $itemId")
                    _saveComplete.value = true
                }
                is AppResult.Error -> {
                    logger.error("DynamicFormViewModel", "Save failed")
                }
            }
            _isSaving.value = false
        }
    }

    companion object {
        private val standardFieldIds = setOf(
            FieldId.NAME, FieldId.USERNAME, FieldId.EMAIL_ADDRESS,
            FieldId.PASSWORD, FieldId.URL, FieldId.NOTES
        )
    }
}
