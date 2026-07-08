package com.passgo.app.feature.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.model.Folder
import com.passgo.app.core.model.Tag
import com.passgo.app.core.model.VaultItem
import com.passgo.app.core.model.VaultItemCategory
import com.passgo.app.data.repository.FolderRepository
import com.passgo.app.data.repository.TagRepository
import com.passgo.app.data.repository.VaultItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject

enum class SortOption { RECENT, NAME, FAVORITE }

sealed class VaultCollection {
    data object AllItems : VaultCollection()
    data object Recent : VaultCollection()
    data object Favorites : VaultCollection()
    data object Archived : VaultCollection()
    data object Trash : VaultCollection()
    data class Category(val category: VaultItemCategory) : VaultCollection()
    data class FolderRef(val folder: Folder) : VaultCollection()
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class VaultViewModel @Inject constructor(
    private val vaultItemRepository: VaultItemRepository,
    private val folderRepository: FolderRepository,
    private val tagRepository: TagRepository,
    private val logger: PassGoLogger
) : ViewModel() {

    private val vaultId = "default"

    companion object {
        private const val PAGE_SIZE = 20
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _debouncedSearchQuery = MutableStateFlow("")

    private val _sortOption = MutableStateFlow(SortOption.RECENT)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _selectedCollection = MutableStateFlow<VaultCollection>(VaultCollection.AllItems)
    val selectedCollection: StateFlow<VaultCollection> = _selectedCollection.asStateFlow()

    private val _selectedTagIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedTagIds: StateFlow<Set<String>> = _selectedTagIds.asStateFlow()

    // Pagination state
    private val _paginatedItems = MutableStateFlow<List<VaultItem>>(emptyList())
    val paginatedItems: StateFlow<List<VaultItem>> = _paginatedItems.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var currentPage = 0

    val folders: StateFlow<List<Folder>> = folderRepository.getActiveFolders(vaultId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tags: StateFlow<List<Tag>> = tagRepository.getActiveTags(vaultId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showCreateFolderDialog = MutableStateFlow(false)
    val showCreateFolderDialog: StateFlow<Boolean> = _showCreateFolderDialog.asStateFlow()

    private val _showRenameFolderDialog = MutableStateFlow<Folder?>(null)
    val showRenameFolderDialog: StateFlow<Folder?> = _showRenameFolderDialog.asStateFlow()

    private val _showDeleteFolderDialog = MutableStateFlow<Folder?>(null)
    val showDeleteFolderDialog: StateFlow<Folder?> = _showDeleteFolderDialog.asStateFlow()

    private val _showCreateTagDialog = MutableStateFlow(false)
    val showCreateTagDialog: StateFlow<Boolean> = _showCreateTagDialog.asStateFlow()

    private val _showDeleteTagDialog = MutableStateFlow<Tag?>(null)
    val showDeleteTagDialog: StateFlow<Tag?> = _showDeleteTagDialog.asStateFlow()

    init {
        _debouncedSearchQuery.value = _searchQuery.value
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { _debouncedSearchQuery.value = it }
        }
        viewModelScope.launch {
            combine(_debouncedSearchQuery, _selectedCollection, _selectedTagIds, _sortOption) { _, _, _, _ -> }
                .collect {
                    _paginatedItems.value = emptyList()
                    currentPage = 0
                    _hasMore.value = true
                }
        }
    }

    val items: StateFlow<List<VaultItem>> = combine(
        _debouncedSearchQuery, _selectedCollection, _selectedTagIds, _sortOption
    ) { query, collection, tagIds, sort ->
        Triple(query, collection, tagIds)
    }.flatMapLatest { (query, collection, tagIds) ->
        val baseFlow = when (collection) {
            is VaultCollection.AllItems -> {
                when {
                    query.isNotBlank() -> vaultItemRepository.searchItems(vaultId, query)
                    else -> when (_sortOption.value) {
                        SortOption.NAME -> vaultItemRepository.getActiveItemsSortedByName(vaultId)
                        SortOption.FAVORITE -> vaultItemRepository.getActiveItemsSortedByFavorite(vaultId)
                        SortOption.RECENT -> vaultItemRepository.getActiveItems(vaultId)
                    }
                }
            }
            is VaultCollection.Recent -> vaultItemRepository.getRecentItems(vaultId)
            is VaultCollection.Favorites -> {
                if (query.isNotBlank()) {
                    vaultItemRepository.searchFavorites(vaultId, query)
                } else {
                    vaultItemRepository.getFavorites(vaultId)
                }
            }
            is VaultCollection.Archived -> vaultItemRepository.getArchivedItems(vaultId)
            is VaultCollection.Trash -> vaultItemRepository.getDeleted(vaultId)
            is VaultCollection.Category -> {
                if (query.isNotBlank()) {
                    vaultItemRepository.searchByType(vaultId, collection.category, query)
                } else {
                    vaultItemRepository.getByType(vaultId, collection.category)
                }
            }
            is VaultCollection.FolderRef -> {
                if (query.isNotBlank()) {
                    vaultItemRepository.searchByFolder(vaultId, collection.folder.id, query)
                } else {
                    vaultItemRepository.getByFolder(collection.folder.id)
                }
            }
        }
        if (tagIds.isNotEmpty()) {
            combine(baseFlow, vaultItemRepository.getItemsByTags(vaultId, tagIds.toList())) { allItems, taggedItems ->
                val taggedIds = taggedItems.map { it.id }.toSet()
                allItems.filter { it.id in taggedIds }
            }
        } else {
            baseFlow
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSortOption(option: SortOption) { _sortOption.value = option }
    fun setCollection(collection: VaultCollection) { _selectedCollection.value = collection }

    fun toggleTagFilter(tagId: String) {
        val current = _selectedTagIds.value
        _selectedTagIds.value = if (tagId in current) current - tagId else current + tagId
    }

    fun clearFilters() {
        _selectedCollection.value = VaultCollection.AllItems
        _selectedTagIds.value = emptySet()
        _searchQuery.value = ""
        _sortOption.value = SortOption.RECENT
    }

    fun loadMoreItems() {
        if (_isLoadingMore.value || !_hasMore.value) return
        _isLoadingMore.value = true
        viewModelScope.launch {
            val offset = (currentPage) * PAGE_SIZE
            val query = _debouncedSearchQuery.value
            val collection = _selectedCollection.value
            val tagIds = _selectedTagIds.value
            val flow: Flow<List<VaultItem>> = when (collection) {
                is VaultCollection.AllItems -> {
                    when {
                        query.isNotBlank() -> vaultItemRepository.searchItemsPaged(vaultId, query, PAGE_SIZE, offset)
                        else -> when (_sortOption.value) {
                            SortOption.NAME -> vaultItemRepository.getActiveItemsSortedByNamePaged(vaultId, PAGE_SIZE, offset)
                            SortOption.FAVORITE -> vaultItemRepository.getActiveItemsSortedByFavoritePaged(vaultId, PAGE_SIZE, offset)
                            SortOption.RECENT -> vaultItemRepository.getActiveItemsPaged(vaultId, PAGE_SIZE, offset)
                        }
                    }
                }
                is VaultCollection.Recent -> vaultItemRepository.getRecentItemsPaged(vaultId, PAGE_SIZE, offset)
                is VaultCollection.Favorites -> {
                    if (query.isNotBlank()) {
                        vaultItemRepository.searchFavoritesPaged(vaultId, query, PAGE_SIZE, offset)
                    } else {
                        vaultItemRepository.getFavoritesPaged(vaultId, PAGE_SIZE, offset)
                    }
                }
                is VaultCollection.Archived -> vaultItemRepository.getArchivedItemsPaged(vaultId, PAGE_SIZE, offset)
                is VaultCollection.Trash -> vaultItemRepository.getDeletedPaged(vaultId, PAGE_SIZE, offset)
                is VaultCollection.Category -> {
                    if (query.isNotBlank()) {
                        vaultItemRepository.searchByTypePaged(vaultId, collection.category, query, PAGE_SIZE, offset)
                    } else {
                        vaultItemRepository.getByTypePaged(vaultId, collection.category, PAGE_SIZE, offset)
                    }
                }
                is VaultCollection.FolderRef -> {
                    if (query.isNotBlank()) {
                        vaultItemRepository.searchByFolderPaged(vaultId, collection.folder.id, query, PAGE_SIZE, offset)
                    } else {
                        vaultItemRepository.getByFolderPaged(collection.folder.id, PAGE_SIZE, offset)
                    }
                }
            }
            val resultFlow: Flow<List<VaultItem>> = if (tagIds.isNotEmpty()) {
                combine(flow, vaultItemRepository.getItemsByTags(vaultId, tagIds.toList())) { pageItems, taggedItems ->
                    val taggedIds = taggedItems.map { it.id }.toSet()
                    pageItems.filter { it.id in taggedIds }
                }
            } else {
                flow
            }
            val page = resultFlow.first()
            _paginatedItems.value = _paginatedItems.value + page
            currentPage++
            _hasMore.value = page.size == PAGE_SIZE
            _isLoadingMore.value = false
        }
    }

    fun toggleFavorite(item: VaultItem) {
        viewModelScope.launch {
            when (vaultItemRepository.update(item.copy(favorite = !item.favorite))) {
                is AppResult.Success -> {}
                is AppResult.Error -> logger.error("VaultViewModel", "Toggle favorite failed")
            }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            when (vaultItemRepository.softDelete(id)) {
                is AppResult.Success -> logger.info("VaultViewModel", "Deleted vault item: $id")
                is AppResult.Error -> logger.error("VaultViewModel", "Delete failed")
            }
        }
    }

    fun permanentDeleteItem(id: String) {
        viewModelScope.launch {
            when (vaultItemRepository.permanentDelete(id)) {
                is AppResult.Success -> logger.info("VaultViewModel", "Permanently deleted item: $id")
                is AppResult.Error -> logger.error("VaultViewModel", "Permanent delete failed")
            }
        }
    }

    fun restoreItem(id: String) {
        viewModelScope.launch {
            when (vaultItemRepository.restore(id)) {
                is AppResult.Success -> logger.info("VaultViewModel", "Restored item: $id")
                is AppResult.Error -> logger.error("VaultViewModel", "Restore failed")
            }
        }
    }

    fun archiveItem(id: String) {
        viewModelScope.launch {
            when (vaultItemRepository.archive(id)) {
                is AppResult.Success -> logger.info("VaultViewModel", "Archived item: $id")
                is AppResult.Error -> logger.error("VaultViewModel", "Archive failed")
            }
        }
    }

    fun unarchiveItem(id: String) {
        viewModelScope.launch {
            when (vaultItemRepository.unarchive(id)) {
                is AppResult.Success -> logger.info("VaultViewModel", "Unarchived item: $id")
                is AppResult.Error -> logger.error("VaultViewModel", "Unarchive failed")
            }
        }
    }

    fun moveItem(itemId: String, folderId: String?) {
        viewModelScope.launch {
            when (vaultItemRepository.moveItem(itemId, folderId)) {
                is AppResult.Success -> logger.info("VaultViewModel", "Moved item $itemId to folder $folderId")
                is AppResult.Error -> logger.error("VaultViewModel", "Move failed")
            }
        }
    }

    fun showCreateFolderDialog() { _showCreateFolderDialog.value = true }
    fun hideCreateFolderDialog() { _showCreateFolderDialog.value = false }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val folder = Folder(
                id = UUID.randomUUID().toString(),
                vaultId = vaultId,
                name = name.trim(),
                sortOrder = (folders.value.size + 1) * 10
            )
            when (folderRepository.insert(folder)) {
                is AppResult.Success -> {
                    logger.info("VaultViewModel", "Created folder: ${folder.id}")
                    _showCreateFolderDialog.value = false
                }
                is AppResult.Error -> logger.error("VaultViewModel", "Create folder failed")
            }
        }
    }

    fun showRenameFolderDialog(folder: Folder) { _showRenameFolderDialog.value = folder }
    fun hideRenameFolderDialog() { _showRenameFolderDialog.value = null }

    fun renameFolder(id: String, newName: String) {
        viewModelScope.launch {
            when (folderRepository.rename(id, newName.trim())) {
                is AppResult.Success -> {
                    logger.info("VaultViewModel", "Renamed folder: $id")
                    _showRenameFolderDialog.value = null
                }
                is AppResult.Error -> logger.error("VaultViewModel", "Rename folder failed")
            }
        }
    }

    fun showDeleteFolderDialog(folder: Folder) { _showDeleteFolderDialog.value = folder }
    fun hideDeleteFolderDialog() { _showDeleteFolderDialog.value = null }

    fun deleteFolder(id: String) {
        viewModelScope.launch {
            when (folderRepository.softDelete(id)) {
                is AppResult.Success -> {
                    logger.info("VaultViewModel", "Deleted folder: $id")
                    _showDeleteFolderDialog.value = null
                    if (_selectedCollection.value is VaultCollection.FolderRef &&
                        (_selectedCollection.value as VaultCollection.FolderRef).folder.id == id
                    ) {
                        _selectedCollection.value = VaultCollection.AllItems
                    }
                }
                is AppResult.Error -> logger.error("VaultViewModel", "Delete folder failed")
            }
        }
    }

    fun showCreateTagDialog() { _showCreateTagDialog.value = true }
    fun hideCreateTagDialog() { _showCreateTagDialog.value = false }

    fun createTag(name: String) {
        viewModelScope.launch {
            val tag = Tag(
                id = UUID.randomUUID().toString(),
                vaultId = vaultId,
                name = name.trim()
            )
            when (tagRepository.insert(tag)) {
                is AppResult.Success -> {
                    logger.info("VaultViewModel", "Created tag: ${tag.id}")
                    _showCreateTagDialog.value = false
                }
                is AppResult.Error -> logger.error("VaultViewModel", "Create tag failed")
            }
        }
    }

    fun showDeleteTagDialog(tag: Tag) { _showDeleteTagDialog.value = tag }
    fun hideDeleteTagDialog() { _showDeleteTagDialog.value = null }

    fun deleteTag(id: String) {
        viewModelScope.launch {
            when (tagRepository.softDelete(id)) {
                is AppResult.Success -> {
                    logger.info("VaultViewModel", "Deleted tag: $id")
                    _showDeleteTagDialog.value = null
                    _selectedTagIds.value = _selectedTagIds.value - id
                }
                is AppResult.Error -> logger.error("VaultViewModel", "Delete tag failed")
            }
        }
    }
}
