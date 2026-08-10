package com.yuuparrow.folderopt.data.model

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data class Scanning(val currentPath: String) : ScanUiState
    data class Success(val root: DirectoryNode) : ScanUiState
    data class Error(val message: String) : ScanUiState
}
