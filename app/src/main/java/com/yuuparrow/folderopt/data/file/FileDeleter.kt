package com.yuuparrow.folderopt.data.file

import android.content.Context
import android.media.MediaScannerConnection
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileDeleter(private val context: Context) {

    data class DeleteResult(val succeeded: List<String>, val failed: List<String>)

    suspend fun delete(paths: List<String>): DeleteResult = withContext(Dispatchers.IO) {
        val succeeded = mutableListOf<String>()
        val failed = mutableListOf<String>()

        for (path in paths) {
            val deleted = try {
                File(path).delete()
            } catch (e: SecurityException) {
                false
            }
            if (deleted) succeeded += path else failed += path
        }

        if (succeeded.isNotEmpty()) {
            MediaScannerConnection.scanFile(context, succeeded.toTypedArray(), null, null)
        }

        DeleteResult(succeeded, failed)
    }
}
