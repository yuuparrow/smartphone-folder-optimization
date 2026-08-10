package com.yuuparrow.folderopt

import com.yuuparrow.folderopt.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun `bytes under 1024 are shown as plain bytes`() {
        assertEquals("0 B", FormatUtils.humanReadableSize(0))
        assertEquals("1023 B", FormatUtils.humanReadableSize(1023))
    }

    @Test
    fun `exactly 1024 bytes is shown as 1_0 KB`() {
        assertEquals("1.0 KB", FormatUtils.humanReadableSize(1024))
    }

    @Test
    fun `megabytes are formatted with one decimal`() {
        assertEquals("1.0 MB", FormatUtils.humanReadableSize(1024L * 1024))
        assertEquals("2.5 MB", FormatUtils.humanReadableSize((2.5 * 1024 * 1024).toLong()))
    }

    @Test
    fun `gigabytes are formatted with one decimal`() {
        assertEquals("1.0 GB", FormatUtils.humanReadableSize(1024L * 1024 * 1024))
    }
}
