package com.yuuparrow.folderopt.ui.storage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuuparrow.folderopt.R
import com.yuuparrow.folderopt.data.model.DirectoryNode
import com.yuuparrow.folderopt.data.model.FileEntry
import com.yuuparrow.folderopt.data.model.ScanUiState
import com.yuuparrow.folderopt.ui.storage.components.Breadcrumb
import com.yuuparrow.folderopt.ui.storage.components.BreadcrumbSegment
import com.yuuparrow.folderopt.ui.storage.components.SizeBar
import com.yuuparrow.folderopt.util.FormatUtils
import java.io.File

@Composable
fun FolderScreen(
    path: String,
    onOpenFolder: (String) -> Unit,
    onBreadcrumbClick: (String) -> Unit,
    viewModel: StorageViewModel = viewModel(factory = StorageViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                    onOpenFolder = onOpenFolder,
                    onBreadcrumbClick = onBreadcrumbClick
                )
            }
        }
    }
}

@Composable
private fun FolderContent(
    root: DirectoryNode,
    node: DirectoryNode,
    onOpenFolder: (String) -> Unit,
    onBreadcrumbClick: (String) -> Unit
) {
    val segments = buildBreadcrumbSegments(root, node)

    Column(modifier = Modifier.fillMaxSize()) {
        Breadcrumb(
            segments = segments,
            onSegmentClick = { onBreadcrumbClick(it.path) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        val childDirs = node.children.sortedByDescending { it.totalSize }
        val childFiles = node.files.sortedByDescending { it.size }
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
                    ratio = file.size.toFloat() / maxSize.toFloat()
                )
            }
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

@Composable
private fun FileRow(file: FileEntry, ratio: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
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
