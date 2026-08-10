package com.yuuparrow.folderopt.data.model

data class FileEntry(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val lastAccessed: Long = 0L
)
