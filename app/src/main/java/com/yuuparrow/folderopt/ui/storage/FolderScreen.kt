package com.yuuparrow.folderopt.ui.storage

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.yuuparrow.folderopt.R
import com.yuuparrow.folderopt.data.file.FileDeleter
import com.yuuparrow.folderopt.data.model.DirectoryNode
import com.yuuparrow.folderopt.data.model.FileEntry
import com.yuuparrow.folderopt.data.model.ScanUiState
import com.yuuparrow.folderopt.data.model.SortField
import com.yuuparrow.folderopt.data.model.SortOption
import com.yuuparrow.folderopt.data.sort.NodeSorter
import com.yuuparrow.folderopt.ui.storage.components.Breadcrumb
import com.yuuparrow.folderopt.ui.storage.components.BreadcrumbSegment
import com.yuuparrow.folderopt.ui.storage.components.SizeBar
import com.yuuparrow.folderopt.util.FormatUtils
import com.yuuparrow.folderopt.util.isMedia
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun FolderScreen(
    path: String,
    onOpenFolder: (String) -> Unit,
    onBreadcrumbClick: (String) -> Unit,
    onOpenPreview: (String) -> Unit,
    viewModel: StorageViewModel = viewModel(factory = StorageViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val selectedPaths by viewModel.selectedPaths.collectAsStateWithLifecycle()

    when (val current = state) {
        is ScanUiState.Idle -> LoadingContent(stringResource(R.string.scan_preparing))
        is ScanUiState.Scanning -> LoadingContent(
            stringResource(R.string.scan_in_progress, current.currentPath)
        )
        is ScanUiState.Error -> ErrorContent(current.message)
        is ScanUiState.Success -> {
            val node = if (path == current.root.path) current.root else viewModel.nodeFor(path)
            if (node == null) {
                ErrorContent(stringResource(R.string.folder_not_found))
            } else {
                FolderContent(
                    root = current.root,
                    node = node,
                    sortOption = sortOption,
                    selectedPaths = selectedPaths,
                    onOpenFolder = onOpenFolder,
                    onBreadcrumbClick = onBreadcrumbClick,
                    onOpenPreview = onOpenPreview,
                    onSetSortOption = viewModel::setSortOption,
                    onToggleSelection = viewModel::toggleSelection,
                    onSelectOnly = viewModel::selectOnly,
                    onClearSelection = viewModel::clearSelection,
                    onDeleteSelected = { viewModel.deleteSelected() }
                )
            }
        }
    }
}

@Composable
private fun FolderContent(
    root: DirectoryNode,
    node: DirectoryNode,
    sortOption: SortOption,
    selectedPaths: Set<String>,
    onOpenFolder: (String) -> Unit,
    onBreadcrumbClick: (String) -> Unit,
    onOpenPreview: (String) -> Unit,
    onSetSortOption: (SortOption) -> Unit,
    onToggleSelection: (String) -> Unit,
    onSelectOnly: (String) -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelected: suspend () -> FileDeleter.DeleteResult
) {
    val segments = buildBreadcrumbSegments(root, node)
    var showConfirmDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val deleteResultTemplate = stringResource(R.string.folder_delete_result)

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Breadcrumb(
                    segments = segments,
                    onSegmentClick = { onBreadcrumbClick(it.path) },
                    modifier = Modifier.weight(1f)
                )
                SortMenuButton(sortOption = sortOption, onSetSortOption = onSetSortOption)
            }

            if (selectedPaths.isNotEmpty()) {
                SelectionBar(
                    count = selectedPaths.size,
                    onCancel = onClearSelection,
                    onDeleteClick = { showConfirmDialog = true }
                )
            }

            val childDirs = NodeSorter.sortDirectories(node.children, sortOption)
            val childFiles = NodeSorter.sortFiles(node.files, sortOption)
            val maxSize = (childDirs.maxOfOrNull { it.totalSize } ?: 0L)
                .coerceAtLeast(childFiles.maxOfOrNull { it.size } ?: 0L)
                .coerceAtLeast(1L)

            if (childDirs.isEmpty() && childFiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.folder_empty))
                }
                return@Column
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(childDirs, key = { it.path }) { child ->
                    DirectoryRow(
                        directory = child,
                        ratio = child.totalSize.toFloat() / maxSize.toFloat(),
                        onClick = { onOpenFolder(child.path) }
                    )
                }
                items(childFiles, key = { it.path }) { file ->
                    FileRow(
                        file = file,
                        ratio = file.size.toFloat() / maxSize.toFloat(),
                        isSelected = file.path in selectedPaths,
                        selectionMode = selectedPaths.isNotEmpty(),
                        onClick = {
                            when {
                                selectedPaths.isNotEmpty() -> onToggleSelection(file.path)
                                file.isMedia() -> onOpenPreview(file.path)
                            }
                        },
                        onLongClick = { onSelectOnly(file.path) }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.folder_confirm_title)) },
            text = { Text(stringResource(R.string.folder_confirm_message, selectedPaths.size)) },
            confirmButton = {
                Button(onClick = {
                    showConfirmDialog = false
                    scope.launch {
                        val result = onDeleteSelected()
                        if (result.failed.isNotEmpty()) {
                            snackbarMessage = String.format(
                                deleteResultTemplate,
                                result.succeeded.size,
                                result.failed.size
                            )
                        }
                    }
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                Button(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SortMenuButton(sortOption: SortOption, onSetSortOption: (SortOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(imageVector = Icons.Filled.Sort, contentDescription = stringResource(R.string.folder_sort_button))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            val fields = listOf(
                SortField.NAME to R.string.folder_sort_name,
                SortField.SIZE to R.string.folder_sort_size,
                SortField.MODIFIED to R.string.folder_sort_modified,
                SortField.ACCESSED to R.string.folder_sort_accessed
            )
            fields.forEach { (field, labelRes) ->
                val isActive = sortOption.field == field
                val arrow = when {
                    !isActive -> ""
                    sortOption.ascending -> " ▲"
                    else -> " ▼"
                }
                DropdownMenuItem(
                    text = { Text(stringResource(labelRes) + arrow) },
                    onClick = {
                        onSetSortOption(
                            if (isActive) {
                                sortOption.copy(ascending = !sortOption.ascending)
                            } else {
                                SortOption(field = field, ascending = field.defaultAscending)
                            }
                        )
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectionBar(count: Int, onCancel: () -> Unit, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancel) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.cancel))
            }
            Text(stringResource(R.string.folder_selected_count, count))
        }
        Button(onClick = onDeleteClick) {
            Text(stringResource(R.string.folder_delete_selected, count))
        }
    }
}

private fun buildBreadcrumbSegments(root: DirectoryNode, node: DirectoryNode): List<BreadcrumbSegment> {
    val rootFile = File(root.path)
    val nodeFile = File(node.path)

    val chain = mutableListOf<File>()
    var current: File? = nodeFile
    while (current != null) {
        chain.add(0, current)
        if (current.path == rootFile.path) break
        current = current.parentFile
    }

    return chain.map { file ->
        val label = if (file.path == rootFile.path) "ストレージ" else file.name
        BreadcrumbSegment(label = label, path = file.path)
    }
}

@Composable
private fun DirectoryRow(directory: DirectoryNode, ratio: Float, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Folder,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(directory.name, style = MaterialTheme.typography.bodyLarge)
            }
            Text(
                FormatUtils.humanReadableSize(directory.totalSize),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        SizeBar(ratio = ratio, modifier = Modifier.padding(top = 6.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileRow(
    file: FileEntry,
    ratio: Float,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                if (file.isMedia()) {
                    AsyncImage(
                        model = File(file.path),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .padding(end = 8.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(file.name, style = MaterialTheme.typography.bodyLarge)
            }
            Text(
                FormatUtils.humanReadableSize(file.size),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        SizeBar(ratio = ratio, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun LoadingContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 12.dp))
            Text(message)
        }
    }
}

@Composable
private fun ErrorContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message)
    }
}
