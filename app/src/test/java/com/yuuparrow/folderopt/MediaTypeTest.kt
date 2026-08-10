package com.yuuparrow.folderopt

import com.yuuparrow.folderopt.util.MediaType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaTypeTest {

    @Test
    fun `recognizes image extensions`() {
        assertTrue(MediaType.isImage("photo.jpg"))
        assertTrue(MediaType.isImage("photo.jpeg"))
        assertTrue(MediaType.isImage("photo.png"))
        assertTrue(MediaType.isImage("photo.webp"))
        assertTrue(MediaType.isImage("photo.heic"))
        assertFalse(MediaType.isVideo("photo.jpg"))
    }

    @Test
    fun `recognizes video extensions`() {
        assertTrue(MediaType.isVideo("clip.mp4"))
        assertTrue(MediaType.isVideo("clip.mov"))
        assertTrue(MediaType.isVideo("clip.mkv"))
        assertFalse(MediaType.isImage("clip.mp4"))
    }

    @Test
    fun `is case-insensitive`() {
        assertTrue(MediaType.isImage("Photo.JPG"))
        assertTrue(MediaType.isVideo("Clip.MP4"))
    }

    @Test
    fun `rejects non-media extensions`() {
        assertFalse(MediaType.isMedia("archive.zip"))
        assertFalse(MediaType.isMedia("notes.txt"))
        assertFalse(MediaType.isMedia("app.apk"))
    }

    @Test
    fun `rejects filenames with no extension`() {
        assertFalse(MediaType.isMedia("README"))
    }

    @Test
    fun `rejects empty filename`() {
        assertFalse(MediaType.isMedia(""))
    }

    @Test
    fun `isMedia is true for either image or video`() {
        assertTrue(MediaType.isMedia("photo.png"))
        assertTrue(MediaType.isMedia("clip.mp4"))
    }
}
