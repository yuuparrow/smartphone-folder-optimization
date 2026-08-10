package com.yuuparrow.folderopt

import com.yuuparrow.folderopt.data.duplicate.DuplicateFinder
import com.yuuparrow.folderopt.data.model.DirectoryNode
import com.yuuparrow.folderopt.data.model.FileEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DuplicateFinderTest {

    private fun file(name: String, path: String, size: Long, lastModified: Long) =
        FileEntry(name = name, path = path, size = size, lastModified = lastModified)

    @Test
    fun `groups files with same name and size across directories`() {
        val childA = DirectoryNode(
            path = "/root/a",
            name = "a",
            totalSize = 100,
            children = emptyList(),
            files = listOf(file("photo.jpg", "/root/a/photo.jpg", 100, 1000L))
        )
        val childB = DirectoryNode(
            path = "/root/b",
            name = "b",
            totalSize = 100,
            children = emptyList(),
            files = listOf(file("photo.jpg", "/root/b/photo.jpg", 100, 2000L))
        )
        val root = DirectoryNode(
            path = "/root",
            name = "root",
            totalSize = 200,
            children = listOf(childA, childB),
            files = emptyList()
        )

        val groups = DuplicateFinder.findDuplicates(root)

        assertEquals(1, groups.size)
        val group = groups.first()
        assertEquals("photo.jpg", group.fileName)
        assertEquals(2, group.files.size)
        // newest (lastModified=2000) should be first
        assertEquals("/root/b/photo.jpg", group.files.first().path)
    }

    @Test
    fun `excludes groups with only a single matching file`() {
        val root = DirectoryNode(
            path = "/root",
            name = "root",
            totalSize = 100,
            children = emptyList(),
            files = listOf(file("unique.txt", "/root/unique.txt", 100, 1000L))
        )

        val groups = DuplicateFinder.findDuplicates(root)

        assertTrue(groups.isEmpty())
    }

    @Test
    fun `does not group same name with different size`() {
        val root = DirectoryNode(
            path = "/root",
            name = "root",
            totalSize = 300,
            children = emptyList(),
            files = listOf(
                file("notes.txt", "/root/notes.txt", 100, 1000L),
                file("notes.txt", "/root/other/notes.txt", 200, 1000L)
            )
        )

        val groups = DuplicateFinder.findDuplicates(root)

        assertTrue(groups.isEmpty())
    }

    @Test
    fun `sorts groups by wasted bytes descending`() {
        val root = DirectoryNode(
            path = "/root",
            name = "root",
            totalSize = 0,
            children = emptyList(),
            files = listOf(
                file("small.txt", "/root/1/small.txt", 10, 1000L),
                file("small.txt", "/root/2/small.txt", 10, 1000L),
                file("big.bin", "/root/1/big.bin", 5000, 1000L),
                file("big.bin", "/root/2/big.bin", 5000, 1000L)
            )
        )

        val groups = DuplicateFinder.findDuplicates(root)

        assertEquals(2, groups.size)
        assertEquals("big.bin", groups.first().fileName)
        assertEquals(5000L, groups.first().wastedBytes)
    }
}
