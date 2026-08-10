package com.yuuparrow.folderopt.util

import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

object FormatUtils {

    private val units = arrayOf("B", "KB", "MB", "GB", "TB")

    fun humanReadableSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val exponent = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceAtMost(units.size - 1)
        val value = bytes / 1024.0.pow(exponent)
        return String.format(Locale.US, "%.1f %s", value, units[exponent])
    }

    fun formatDate(epochMillis: Long): String {
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))
    }
}
