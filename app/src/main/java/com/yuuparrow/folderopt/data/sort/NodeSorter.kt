package com.yuuparrow.folderopt.data.sort

import com.yuuparrow.folderopt.data.model.DirectoryNode
import com.yuuparrow.folderopt.data.model.FileEntry
import com.yuuparrow.folderopt.data.model.SortField
import com.yuuparrow.folderopt.data.model.SortOption

object NodeSorter {

    fun sortDirectories(dirs: List<DirectoryNode>, option: SortOption): List<DirectoryNode> {
        val comparator: Comparator<DirectoryNode> = when (option.field) {
            SortField.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortField.SIZE -> compareBy { it.totalSize }
            SortField.MODIFIED -> compareBy { it.lastModified }
            SortField.ACCESSED -> compareBy { it.lastAccessed }
        }
        return dirs.sortedWith(if (option.ascending) comparator else comparator.reversed())
    }

    fun sortFiles(files: List<FileEntry>, option: SortOption): List<FileEntry> {
        val comparator: Comparator<FileEntry> = when (option.field) {
            SortField.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortField.SIZE -> compareBy { it.size }
            SortField.MODIFIED -> compareBy { it.lastModified }
            SortField.ACCESSED -> compareBy { it.lastAccessed }
        }
        return files.sortedWith(if (option.ascending) comparator else comparator.reversed())
    }
}
