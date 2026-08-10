package com.yuuparrow.folderopt.data.model

data class DirectoryNode(
    val path: String,
    val name: String,
    val totalSize: Long,
    val children: List<DirectoryNode>,
    val files: List<FileEntry>
)

fun DirectoryNode.findNode(targetPath: String): DirectoryNode? {
    if (path == targetPath) return this
    for (child in children) {
        child.findNode(targetPath)?.let { return it }
    }
    return null
}
