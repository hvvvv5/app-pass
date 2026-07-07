package com.passgo.app.feature.vault

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.passgo.app.core.model.Folder
import com.passgo.app.core.model.Tag
import com.passgo.app.core.model.VaultItem
import com.passgo.app.core.model.VaultItemCategory
import com.passgo.app.core.ui.components.CategoryIcon

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaultScreen(
    onAddItem: () -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val selectedCollection by viewModel.selectedCollection.collectAsState()
    val selectedTagIds by viewModel.selectedTagIds.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val tags by viewModel.tags.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showCollectionMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showPermanentDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showRestoreDialog by remember { mutableStateOf<String?>(null) }
    var showArchiveDialog by remember { mutableStateOf<String?>(null) }
    var showUnarchiveDialog by remember { mutableStateOf<String?>(null) }
    var showMoveToFolderDialog by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Search items") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        collectionDisplayName(selectedCollection),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.combinedClickable(
                            onClick = { showCollectionMenu = true }
                        )
                    )
                    DropdownMenu(
                        expanded = showCollectionMenu,
                        onDismissRequest = { showCollectionMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Items") },
                            onClick = {
                                viewModel.setCollection(VaultCollection.AllItems)
                                showCollectionMenu = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.Folder, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Recent") },
                            onClick = {
                                viewModel.setCollection(VaultCollection.Recent)
                                showCollectionMenu = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.StarBorder, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Favorites") },
                            onClick = {
                                viewModel.setCollection(VaultCollection.Favorites)
                                showCollectionMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Archived") },
                            onClick = {
                                viewModel.setCollection(VaultCollection.Archived)
                                showCollectionMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Trash") },
                            onClick = {
                                viewModel.setCollection(VaultCollection.Trash)
                                showCollectionMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                        HorizontalDivider()
                        Text(
                            "Categories",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        VaultItemCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName) },
                                onClick = {
                                    viewModel.setCollection(VaultCollection.Category(category))
                                    showCollectionMenu = false
                                }
                            )
                        }
                        if (folders.isNotEmpty()) {
                            HorizontalDivider()
                            Text(
                                "Folders",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            folders.forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text(folder.name) },
                                    onClick = {
                                        viewModel.setCollection(VaultCollection.FolderRef(folder))
                                        showCollectionMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) }
                                )
                            }
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Create Folder") },
                            onClick = {
                                viewModel.showCreateFolderDialog()
                                showCollectionMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                        )
                    }
                }

                Row {
                    Text(
                        "Sort: ${sortOption.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.combinedClickable(onClick = { showSortMenu = true })
                    )
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    viewModel.setSortOption(option)
                                    showSortMenu = false
                                }
                            )
                        }
                    }

                    if (selectedCollection !is VaultCollection.AllItems || selectedTagIds.isNotEmpty()) {
                        TextButton(onClick = viewModel::clearFilters) {
                            Text("Clear")
                        }
                    }
                }
            }

            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        FilterChip(
                            selected = tag.id in selectedTagIds,
                            onClick = { viewModel.toggleTagFilter(tag.id) },
                            label = { Text(tag.name) }
                        )
                    }
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.showCreateTagDialog() },
                        label = { Text("+ Tag") }
                    )
                }
            }

            if (selectedCollection is VaultCollection.FolderRef) {
                val folder = (selectedCollection as VaultCollection.FolderRef).folder
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { viewModel.showRenameFolderDialog(folder) }) {
                        Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rename")
                    }
                    OutlinedButton(onClick = { viewModel.showDeleteFolderDialog(folder) }) {
                        Icon(Icons.Default.FolderOff, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            emptyStateTitle(selectedCollection, searchQuery.isNotEmpty()),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            emptyStateHint(selectedCollection),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            collection = selectedCollection,
                            onClick = { onItemClick(item.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(item) },
                            onDelete = { showDeleteDialog = item.id },
                            onPermanentDelete = { showPermanentDeleteDialog = item.id },
                            onRestore = { showRestoreDialog = item.id },
                            onArchive = { showArchiveDialog = item.id },
                            onUnarchive = { showUnarchiveDialog = item.id },
                            onMoveToFolder = { showMoveToFolderDialog = item.id }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddItem,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add item")
        }
    }

    showDeleteDialog?.let { itemId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Move to Trash") },
            text = { Text("Move this item to trash? You can restore it later.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteItem(itemId)
                    showDeleteDialog = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }

    showPermanentDeleteDialog?.let { itemId ->
        AlertDialog(
            onDismissRequest = { showPermanentDeleteDialog = null },
            title = { Text("Permanently Delete") },
            text = { Text("This action cannot be undone. Delete this item permanently?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.permanentDeleteItem(itemId)
                    showPermanentDeleteDialog = null
                }) { Text("Delete Forever") }
            },
            dismissButton = {
                TextButton(onClick = { showPermanentDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }

    showRestoreDialog?.let { itemId ->
        AlertDialog(
            onDismissRequest = { showRestoreDialog = null },
            title = { Text("Restore Item") },
            text = { Text("Restore this item to your vault?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.restoreItem(itemId)
                    showRestoreDialog = null
                }) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = null }) { Text("Cancel") }
            }
        )
    }

    showArchiveDialog?.let { itemId ->
        AlertDialog(
            onDismissRequest = { showArchiveDialog = null },
            title = { Text("Archive Item") },
            text = { Text("Archive this item? It will be hidden from your main vault.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.archiveItem(itemId)
                    showArchiveDialog = null
                }) { Text("Archive") }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = null }) { Text("Cancel") }
            }
        )
    }

    showUnarchiveDialog?.let { itemId ->
        AlertDialog(
            onDismissRequest = { showUnarchiveDialog = null },
            title = { Text("Unarchive Item") },
            text = { Text("Restore this item to your main vault?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.unarchiveItem(itemId)
                    showUnarchiveDialog = null
                }) { Text("Unarchive") }
            },
            dismissButton = {
                TextButton(onClick = { showUnarchiveDialog = null }) { Text("Cancel") }
            }
        )
    }

    showMoveToFolderDialog?.let { itemId ->
        FolderSelectDialog(
            folders = folders,
            onSelect = { folderId ->
                viewModel.moveItem(itemId, folderId)
                showMoveToFolderDialog = null
            },
            onDismiss = { showMoveToFolderDialog = null }
        )
    }

    if (viewModel.showCreateFolderDialog.value) {
        CreateFolderDialog(
            onConfirm = viewModel::createFolder,
            onDismiss = viewModel::hideCreateFolderDialog
        )
    }

    viewModel.showRenameFolderDialog.value?.let { folder ->
        RenameFolderDialog(
            currentName = folder.name,
            onConfirm = { newName -> viewModel.renameFolder(folder.id, newName) },
            onDismiss = viewModel::hideRenameFolderDialog
        )
    }

    viewModel.showDeleteFolderDialog.value?.let { folder ->
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteFolderDialog,
            title = { Text("Delete Folder") },
            text = { Text("Delete folder \"${folder.name}\"? Items in this folder will not be deleted.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteFolder(folder.id) }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDeleteFolderDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    if (viewModel.showCreateTagDialog.value) {
        CreateTagDialog(
            onConfirm = viewModel::createTag,
            onDismiss = viewModel::hideCreateTagDialog
        )
    }

    viewModel.showDeleteTagDialog.value?.let { tag ->
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteTagDialog,
            title = { Text("Delete Tag") },
            text = { Text("Delete tag \"${tag.name}\"? It will be removed from all items.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTag(tag.id) }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDeleteTagDialog) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FolderSelectDialog(
    folders: List<Folder>,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move to Folder") },
        text = {
            Column {
                TextButton(onClick = { onSelect(null) }, modifier = Modifier.fillMaxWidth()) {
                    Text("No folder")
                }
                folders.forEach { folder ->
                    TextButton(
                        onClick = { onSelect(folder.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(folder.name)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CreateFolderDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Folder") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Folder name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RenameFolderDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Folder") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Folder name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank() && name != currentName
            ) { Text("Rename") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CreateTagDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Tag") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tag name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemCard(
    item: VaultItem,
    collection: VaultCollection,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onPermanentDelete: () -> Unit,
    onRestore: () -> Unit,
    onArchive: () -> Unit,
    onUnarchive: () -> Unit,
    onMoveToFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(category = item.category)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.username.isNotEmpty()) {
                    Text(
                        item.username,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (item.favorite) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (item.favorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (item.favorite) "Unfavorite" else "Favorite",
                    tint = if (item.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            when (collection) {
                VaultCollection.Trash -> {
                    DropdownMenuItem(
                        text = { Text("Restore") },
                        onClick = { onRestore(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.RestoreFromTrash, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Forever") },
                        onClick = { onPermanentDelete(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
                VaultCollection.Archived -> {
                    DropdownMenuItem(
                        text = { Text("Unarchive") },
                        onClick = { onUnarchive(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Unarchive, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { onDelete(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
                else -> {
                    DropdownMenuItem(
                        text = { Text("Archive") },
                        onClick = { onArchive(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Move to Folder") },
                        onClick = { onMoveToFolder(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { onDelete(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }
}

private fun collectionDisplayName(collection: VaultCollection): String = when (collection) {
    VaultCollection.AllItems -> "All Items"
    VaultCollection.Recent -> "Recent"
    VaultCollection.Favorites -> "Favorites"
    VaultCollection.Archived -> "Archived"
    VaultCollection.Trash -> "Trash"
    is VaultCollection.Category -> collection.category.displayName
    is VaultCollection.FolderRef -> collection.folder.name
}

private fun emptyStateTitle(collection: VaultCollection, isSearching: Boolean): String = when {
    isSearching -> "No matching items"
    collection is VaultCollection.Trash -> "Trash is empty"
    collection is VaultCollection.Archived -> "No archived items"
    collection is VaultCollection.Favorites -> "No favorites yet"
    collection is VaultCollection.Recent -> "No recent items"
    else -> "No items"
}

private fun emptyStateHint(collection: VaultCollection): String = when {
    collection is VaultCollection.Trash -> "Deleted items appear here"
    collection is VaultCollection.Archived -> "Archived items appear here"
    collection is VaultCollection.Favorites -> "Tap the star on an item to favorite it"
    collection is VaultCollection.Recent -> "Recently updated items appear here"
    collection is VaultCollection.AllItems -> "Tap + to add your first item"
    else -> "No items in this collection"
}
