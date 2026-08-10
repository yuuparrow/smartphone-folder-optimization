package com.yuuparrow.folderopt.ui.navigation

import android.net.Uri

object Routes {
    const val PERMISSION = "permission"

    const val FOLDER_PATTERN = "folder/{path}"
    fun folder(path: String) = "folder/${Uri.encode(path)}"

    const val DUPLICATES = "duplicates"

    const val DUPLICATE_GROUP_PATTERN = "duplicates/group/{key}"
    fun duplicateGroup(key: String) = "duplicates/group/${Uri.encode(key)}"
}
