package com.yuuparrow.folderopt.data.model

data class DuplicateGroup(
    val key: String,
    val fileName: String,
    val fileSize: Long,
    val files: List<FileEntry>
) {
    val wastedBytes: Long get() = fileSize * (files.size - 1)
}
