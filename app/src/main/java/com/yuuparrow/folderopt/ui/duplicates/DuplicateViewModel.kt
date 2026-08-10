package com.yuuparrow.folderopt.ui.duplicates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuuparrow.folderopt.data.duplicate.DuplicateFinder
import com.yuuparrow.folderopt.data.file.FileDeleter
import com.yuuparrow.folderopt.data.model.DuplicateGroup
import com.yuuparrow.folderopt.data.model.ScanUiState
import com.yuuparrow.folderopt.data.repository.StorageRepository
import com.yuuparrow.folderopt.ui.storage.extrasApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DuplicateViewModel(
    private val repository: StorageRepository,
    private val fileDeleter: FileDeleter
) : ViewModel() {

    private val _selection = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val selection: StateFlow<Map<String, Set<String>>> = _selection.asStateFlow()

    val scanState: StateFlow<ScanUiState> = repository.state

    val duplicateGroups: StateFlow<List<DuplicateGroup>> = repository.state
        .map { scanState ->
            if (scanState is ScanUiState.Success) {
                DuplicateFinder.findDuplicates(scanState.root)
            } else {
                emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectionFor(
        group: DuplicateGroup,
        selectionMap: Map<String, Set<String>> = _selection.value
    ): Set<String> {
        return selectionMap[group.key] ?: defaultSelection(group)
    }

    private fun defaultSelection(group: DuplicateGroup): Set<String> {
        // 既定では最新のファイル(先頭)を残し、それ以外を削除候補にする
        return group.files.drop(1).map { it.path }.toSet()
    }

    fun toggleSelection(group: DuplicateGroup, path: String) {
        val current = selectionFor(group)
        val updated = if (path in current) current - path else current + path
        _selection.value = _selection.value + (group.key to updated)
    }

    fun scan() {
        viewModelScope.launch { repository.scan() }
    }

    suspend fun deleteSelected(group: DuplicateGroup): FileDeleter.DeleteResult {
        val paths = selectionFor(group).toList()
        val result = fileDeleter.delete(paths)
        if (result.succeeded.isNotEmpty()) {
            repository.removeFiles(result.succeeded.toSet())
            _selection.value = _selection.value - group.key
        }
        return result
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = extrasApplication()
                DuplicateViewModel(app.container.storageRepository, app.container.fileDeleter)
            }
        }
    }
}
