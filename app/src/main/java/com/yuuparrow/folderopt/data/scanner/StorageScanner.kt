package com.yuuparrow.folderopt.data.scanner

import com.yuuparrow.folderopt.data.model.DirectoryNode
import com.yuuparrow.folderopt.data.model.FileEntry
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageScanner {

    suspend fun scan(
        rootPath: String,
        onProgress: (String) -> Unit = {}
    ): DirectoryNode = withContext(Dispatchers.IO) {
        var counter = 0
        scanDir(File(rootPath)) { path ->
            counter++
            if (counter % 40 == 0) onProgress(path)
        }
    }

    private fun scanDir(dir: File, onProgress: (String) -> Unit): DirectoryNode {
        onProgress(dir.absolutePath)
        val children = mutableListOf<DirectoryNode>()
        val files = mutableListOf<FileEntry>()
        var total = 0L

        val listed = try {
            dir.listFiles()
        } catch (e: SecurityException) {
            null
        } ?: emptyArray()

        for (entry in listed) {
            try {
                when {
                    entry.isDirectory -> {
                        val child = scanDir(entry, onProgress)
                        children += child
                        total += child.totalSize
                    }
                    entry.isFile -> {
                        val size = entry.length()
                        files += FileEntry(entry.name, entry.absolutePath, size, entry.lastModified())
                        total += size
                    }
                }
            } catch (e: SecurityException) {
                // 他アプリの Android/data 等、アクセス権のないエントリはスキップする
            }
        }

        return DirectoryNode(
            path = dir.absolutePath,
            name = dir.name.ifEmpty { dir.absolutePath },
            totalSize = total,
            children = children,
            files = files
        )
    }
}
