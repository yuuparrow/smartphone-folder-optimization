package com.yuuparrow.folderopt.ui.duplicates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
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
import com.yuuparrow.folderopt.data.model.DuplicateGroup
import com.yuuparrow.folderopt.data.model.ScanUiState
import com.yuuparrow.folderopt.util.FormatUtils

@Composable
fun DuplicateListScreen(
    onOpenGroup: (String) -> Unit,
    viewModel: DuplicateViewModel = viewModel(factory = DuplicateViewModel.Factory)
) {
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val groups by viewModel.duplicateGroups.collectAsStateWithLifecycle()

    when (scanState) {
        is ScanUiState.Idle, is ScanUiState.Scanning -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.padding(bottom = 12.dp))
                    Text(stringResource(R.string.duplicates_scanning))
                }
            }
        }
        is ScanUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.duplicates_scan_failed))
                    Button(onClick = { viewModel.scan() }, modifier = Modifier.padding(top = 12.dp)) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
        is ScanUiState.Success -> {
            if (groups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.duplicates_none_found))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(groups, key = { it.key }) { group ->
                        DuplicateGroupCard(group = group, onClick = { onOpenGroup(group.key) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DuplicateGroupCard(group: DuplicateGroup, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(group.fileName, style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.duplicates_count, group.files.size),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    stringResource(
                        R.string.duplicates_wasted,
                        FormatUtils.humanReadableSize(group.wastedBytes)
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
