package com.yuuparrow.folderopt.util

import com.yuuparrow.folderopt.data.model.FileEntry

object MediaType {
    private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif")
    private val videoExtensions = setOf("mp4", "mkv", "webm", "3gp", "3gpp", "mov", "avi", "m4v")

    fun isImage(name: String): Boolean = extensionOf(name) in imageExtensions
    fun isVideo(name: String): Boolean = extensionOf(name) in videoExtensions
    fun isMedia(name: String): Boolean = isImage(name) || isVideo(name)

    private fun extensionOf(name: String): String =
        name.substringAfterLast('.', "").lowercase()
}

fun FileEntry.isImage(): Boolean = MediaType.isImage(name)
fun FileEntry.isVideo(): Boolean = MediaType.isVideo(name)
fun FileEntry.isMedia(): Boolean = MediaType.isMedia(name)
