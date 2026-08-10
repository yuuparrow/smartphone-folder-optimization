package com.yuuparrow.folderopt

import com.yuuparrow.folderopt.data.model.DirectoryNode
import com.yuuparrow.folderopt.data.model.FileEntry
import com.yuuparrow.folderopt.data.model.SortField
import com.yuuparrow.folderopt.data.model.SortOption
import com.yuuparrow.folderopt.data.sort.NodeSorter
import org.junit.Assert.assertEquals
import org.junit.Test

class NodeSorterTest {

    private fun file(name: String, size: Long, modified: Long, accessed: Long) =
        FileEntry(name = name, path = "/root/$name", size = size, lastModified = modified, lastAccessed = accessed)

    private fun dir(name: String, size: Long, modified: Long, accessed: Long) =
        DirectoryNode(
            path = "/root/$name",
            name = name,
            totalSize = size,
            children = emptyList(),
            files = emptyList(),
            lastModified = modified,
            lastAccessed = accessed
        )

    // size, modified, and accessed are deliberately given DIFFERENT relative orderings
    // across the three files, so a test that mixes up which field is being sorted on
    // (e.g. sorting by size when MODIFIED was requested) will fail rather than pass by
    // coincidence.
    private val files = listOf(
        file("banana.txt", size = 200, modified = 2000L, accessed = 500L),
        file("Apple.txt", size = 100, modified = 3000L, accessed = 900L),
        file("cherry.txt", size = 300, modified = 1000L, accessed = 100L)
    )

    private val dirs = listOf(
        dir("banana", size = 200, modified = 2000L, accessed = 500L),
        dir("Apple", size = 100, modified = 3000L, accessed = 900L),
        dir("cherry", size = 300, modified = 1000L, accessed = 100L)
    )

    @Test
    fun `sorts files by name case-insensitively`() {
        val ascending = NodeSorter.sortFiles(files, SortOption(SortField.NAME, ascending = true))
        assertEquals(listOf("Apple.txt", "banana.txt", "cherry.txt"), ascending.map { it.name })

        val descending = NodeSorter.sortFiles(files, SortOption(SortField.NAME, ascending = false))
        assertEquals(listOf("cherry.txt", "banana.txt", "Apple.txt"), descending.map { it.name })
    }

    @Test
    fun `sorts files by size`() {
        val ascending = NodeSorter.sortFiles(files, SortOption(SortField.SIZE, ascending = true))
        assertEquals(listOf("Apple.txt", "banana.txt", "cherry.txt"), ascending.map { it.name })

        val descending = NodeSorter.sortFiles(files, SortOption(SortField.SIZE, ascending = false))
        assertEquals(listOf("cherry.txt", "banana.txt", "Apple.txt"), descending.map { it.name })
    }

    @Test
    fun `sorts files by last modified`() {
        val ascending = NodeSorter.sortFiles(files, SortOption(SortField.MODIFIED, ascending = true))
        assertEquals(listOf("cherry.txt", "banana.txt", "Apple.txt"), ascending.map { it.name })
    }

    @Test
    fun `sorts files by last accessed`() {
        val ascending = NodeSorter.sortFiles(files, SortOption(SortField.ACCESSED, ascending = true))
        assertEquals(listOf("cherry.txt", "banana.txt", "Apple.txt"), ascending.map { it.name })
    }

    @Test
    fun `sorts directories the same way as files`() {
        val byName = NodeSorter.sortDirectories(dirs, SortOption(SortField.NAME, ascending = true))
        assertEquals(listOf("Apple", "banana", "cherry"), byName.map { it.name })

        val bySize = NodeSorter.sortDirectories(dirs, SortOption(SortField.SIZE, ascending = false))
        assertEquals(listOf("cherry", "banana", "Apple"), bySize.map { it.name })

        val byModified = NodeSorter.sortDirectories(dirs, SortOption(SortField.MODIFIED, ascending = true))
        assertEquals(listOf("cherry", "banana", "Apple"), byModified.map { it.name })

        val byAccessed = NodeSorter.sortDirectories(dirs, SortOption(SortField.ACCESSED, ascending = true))
        assertEquals(listOf("cherry", "banana", "Apple"), byAccessed.map { it.name })
    }

    @Test
    fun `default sort option matches previous size-descending behavior`() {
        val default = NodeSorter.sortFiles(files, SortOption())
        assertEquals(listOf("cherry.txt", "banana.txt", "Apple.txt"), default.map { it.name })
    }

    @Test
    fun `ties keep stable relative order`() {
        val tied = listOf(
            file("a.txt", size = 100, modified = 100L, accessed = 100L),
            file("b.txt", size = 100, modified = 100L, accessed = 100L)
        )
        val result = NodeSorter.sortFiles(tied, SortOption(SortField.SIZE, ascending = true))
        assertEquals(listOf("a.txt", "b.txt"), result.map { it.name })
    }
}
