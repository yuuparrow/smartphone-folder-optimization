package com.yuuparrow.folderopt.data.scanner

import android.system.ErrnoException
import android.system.Os
import com.yuuparrow.folderopt.data.model.DirectoryNode
import com.yuuparrow.folderopt.data.model.FileEntry
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageScanner {

    private data class StatResult(val size: Long, val lastModified: Long, val lastAccessed: Long)

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
                        val stat = statOrFallback(entry)
                        files += FileEntry(entry.name, entry.absolutePath, stat.size, stat.lastModified, stat.lastAccessed)
                        total += stat.size
                    }
                }
            } catch (e: SecurityException) {
                // 他アプリの Android/data 等、アクセス権のないエントリはスキップする
            }
        }

        val dirStat = statOrFallback(dir)
        return DirectoryNode(
            path = dir.absolutePath,
            name = dir.name.ifEmpty { dir.absolutePath },
            totalSize = total,
            children = children,
            files = files,
            lastModified = dirStat.lastModified,
            lastAccessed = dirStat.lastAccessed
        )
    }

    private fun statOrFallback(file: File): StatResult {
        return try {
            val stat = Os.stat(file.absolutePath)
            StatResult(stat.st_size, stat.st_mtime * 1000L, stat.st_atime * 1000L)
        } catch (e: ErrnoException) {
            val fallback = file.lastModified()
            StatResult(file.length(), fallback, fallback)
        }
    }
}
