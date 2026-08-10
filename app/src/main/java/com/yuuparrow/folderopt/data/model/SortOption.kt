package com.yuuparrow.folderopt.data.model

enum class SortField(val defaultAscending: Boolean) {
    NAME(true),
    SIZE(false),
    MODIFIED(false),
    ACCESSED(false)
}

data class SortOption(
    val field: SortField = SortField.SIZE,
    val ascending: Boolean = false
)
