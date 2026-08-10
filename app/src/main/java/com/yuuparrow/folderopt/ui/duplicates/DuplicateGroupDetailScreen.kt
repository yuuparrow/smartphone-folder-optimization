package com.yuuparrow.folderopt.ui.duplicates

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuuparrow.folderopt.R
import com.yuuparrow.folderopt.data.model.DuplicateGroup
import com.yuuparrow.folderopt.data.model.FileEntry
import com.yuuparrow.folderopt.util.FormatUtils
import kotlinx.coroutines.launch

@Composable
fun DuplicateGroupDetailScreen(
    groupKey: String,
    onDone: () -> Unit,
    viewModel: DuplicateViewModel = viewModel(factory = DuplicateViewModel.Factory)
) {
    val groups by viewModel.duplicateGroups.collectAsStateWithLifecycle()
    val group = groups.firstOrNull { it.key == groupKey }

    if (group == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.folder_not_found))
        }
        return
    }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val selectionMap by viewModel.selection.collectAsStateWithLifecycle()
    val selection = viewModel.selectionFor(group, selectionMap)

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = group.fileName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(modifier = Modifier.weight(1f, fill = true)) {
                items(group.files, key = { it.path }) { file ->
                    FileSelectionRow(
                        file = file,
                        checked = file.path in selection,
                        onCheckedChange = { viewModel.toggleSelection(group, file.path) }
                    )
                }
            }
            Button(
                onClick = { showConfirmDialog = true },
                enabled = selection.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.duplicates_delete_selected, selection.size))
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
            title = { Text(stringResource(R.string.duplicates_confirm_title)) },
            text = { Text(stringResource(R.string.duplicates_confirm_message, selection.size)) },
            confirmButton = {
                Button(onClick = {
                    showConfirmDialog = false
                    scope.launch {
                        val result = viewModel.deleteSelected(group)
                        snackbarMessage = if (result.failed.isEmpty()) {
                            onDone()
                            null
                        } else {
                            "削除完了: ${result.succeeded.size}件 / 失敗: ${result.failed.size}件"
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
private fun FileSelectionRow(file: FileEntry, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(file.path, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${FormatUtils.humanReadableSize(file.size)} · ${FormatUtils.formatDate(file.lastModified)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
