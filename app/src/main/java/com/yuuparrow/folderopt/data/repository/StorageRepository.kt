package com.yuuparrow.folderopt.data.repository

import android.os.Environment
import com.yuuparrow.folderopt.data.model.DirectoryNode
import com.yuuparrow.folderopt.data.model.ScanUiState
import com.yuuparrow.folderopt.data.model.SortOption
import com.yuuparrow.folderopt.data.model.findNode
import com.yuuparrow.folderopt.data.scanner.StorageScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StorageRepository(private val scanner: StorageScanner) {

    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption())
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    val rootPath: String
        get() = Environment.getExternalStorageDirectory().absolutePath

    suspend fun scan(rootPath: String = this.rootPath) {
        _state.value = ScanUiState.Scanning(rootPath)
        try {
            val root = scanner.scan(rootPath) { path ->
                _state.value = ScanUiState.Scanning(path)
            }
            _state.value = ScanUiState.Success(root)
        } catch (e: Exception) {
            _state.value = ScanUiState.Error(e.message ?: "scan failed")
        }
    }

    fun findNode(path: String): DirectoryNode? {
        val current = _state.value
        if (current !is ScanUiState.Success) return null
        return current.root.findNode(path)
    }

    /** 削除後に呼び、キャッシュ済みツリーから該当パスを取り除く。 */
    fun removeFiles(paths: Set<String>) {
        val current = _state.value
        if (current !is ScanUiState.Success) return
        _state.value = ScanUiState.Success(removeFilesFromNode(current.root, paths))
    }

    private fun removeFilesFromNode(node: DirectoryNode, paths: Set<String>): DirectoryNode {
        val remainingFiles = node.files.filterNot { it.path in paths }
        val removedSize = node.files.filter { it.path in paths }.sumOf { it.size }
        val newChildren = node.children.map { removeFilesFromNode(it, paths) }
        val childSizeDelta = node.children.zip(newChildren).sumOf { (old, new) -> old.totalSize - new.totalSize }
        return node.copy(
            files = remainingFiles,
            children = newChildren,
            totalSize = node.totalSize - removedSize - childSizeDelta
        )
    }
}
