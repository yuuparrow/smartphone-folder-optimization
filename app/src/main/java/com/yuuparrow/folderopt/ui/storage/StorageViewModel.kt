package com.yuuparrow.folderopt.ui.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuuparrow.folderopt.FolderOptApplication
import com.yuuparrow.folderopt.data.file.FileDeleter
import com.yuuparrow.folderopt.data.model.DirectoryNode
import com.yuuparrow.folderopt.data.model.ScanUiState
import com.yuuparrow.folderopt.data.model.SortOption
import com.yuuparrow.folderopt.data.repository.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StorageViewModel(
    private val repository: StorageRepository,
    private val fileDeleter: FileDeleter
) : ViewModel() {

    val state: StateFlow<ScanUiState> = repository.state
    val sortOption: StateFlow<SortOption> = repository.sortOption
    val rootPath: String get() = repository.rootPath

    private val _selectedPaths = MutableStateFlow<Set<String>>(emptySet())
    val selectedPaths: StateFlow<Set<String>> = _selectedPaths.asStateFlow()

    init {
        scan()
    }

    fun scan() {
        viewModelScope.launch {
            repository.scan()
        }
    }

    fun nodeFor(path: String): DirectoryNode? = repository.findNode(path)

    fun setSortOption(option: SortOption) = repository.setSortOption(option)

    fun toggleSelection(path: String) {
        _selectedPaths.value = if (path in _selectedPaths.value) {
            _selectedPaths.value - path
        } else {
            _selectedPaths.value + path
        }
    }

    /** 長押しで選択モードに入る際、そのファイルだけを選択状態にする。 */
    fun selectOnly(path: String) {
        _selectedPaths.value = setOf(path)
    }

    fun clearSelection() {
        _selectedPaths.value = emptySet()
    }

    suspend fun deleteSelected(): FileDeleter.DeleteResult {
        val paths = _selectedPaths.value.toList()
        val result = fileDeleter.delete(paths)
        if (result.succeeded.isNotEmpty()) {
            repository.removeFiles(result.succeeded.toSet())
        }
        _selectedPaths.value = emptySet()
        return result
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = extrasApplication()
                StorageViewModel(app.container.storageRepository, app.container.fileDeleter)
            }
        }
    }
}

internal fun CreationExtras.extrasApplication(): FolderOptApplication {
    val application = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
    return application as FolderOptApplication
}
