package com.yuuparrow.folderopt.ui.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yuuparrow.folderopt.FolderOptApplication
import com.yuuparrow.folderopt.data.model.DirectoryNode
import com.yuuparrow.folderopt.data.model.ScanUiState
import com.yuuparrow.folderopt.data.repository.StorageRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StorageViewModel(private val repository: StorageRepository) : ViewModel() {

    val state: StateFlow<ScanUiState> = repository.state
    val rootPath: String get() = repository.rootPath

    init {
        scan()
    }

    fun scan() {
        viewModelScope.launch {
            repository.scan()
        }
    }

    fun nodeFor(path: String): DirectoryNode? = repository.findNode(path)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = extrasApplication()
                StorageViewModel(app.container.storageRepository)
            }
        }
    }
}

internal fun CreationExtras.extrasApplication(): FolderOptApplication {
    val application = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
    return application as FolderOptApplication
}
