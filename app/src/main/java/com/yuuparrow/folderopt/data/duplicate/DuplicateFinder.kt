package com.yuuparrow.folderopt.data.duplicate

import com.yuuparrow.folderopt.data.model.DirectoryNode
import com.yuuparrow.folderopt.data.model.DuplicateGroup
import com.yuuparrow.folderopt.data.model.FileEntry

object DuplicateFinder {

    fun findDuplicates(root: DirectoryNode): List<DuplicateGroup> {
        val all = mutableListOf<FileEntry>()
        collect(root, all)
        return all.groupBy { it.name to it.size }
            .filter { it.value.size > 1 }
            .map { (key, files) ->
                DuplicateGroup(
                    key = "${key.first}|${key.second}",
                    fileName = key.first,
                    fileSize = key.second,
                    files = files.sortedByDescending { it.lastModified }
                )
            }
            .sortedByDescending { it.wastedBytes }
    }

    private fun collect(node: DirectoryNode, out: MutableList<FileEntry>) {
        out += node.files
        node.children.forEach { collect(it, out) }
    }
}
