package com.elensiel.camer_

import com.elensiel.camer_.data.purgeStaleCache
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CacheCleanerTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun deletesPhotoFiles_keepsEverythingElse() {
        val photos = listOf("a.jpg", "b.PNG", "c.webp", "d.dng").map { tmp.newFile(it) }
        val other = tmp.newFile("keep.txt")
        val subDir = tmp.newFolder("sub.jpg") // directory with photo-like name

        purgeStaleCache(tmp.root)

        photos.forEach { assertFalse("${it.name} should be deleted", it.exists()) }
        assertTrue(other.exists())
        assertTrue(subDir.exists())
    }

    @Test
    fun emptyDir_doesNotThrow() = purgeStaleCache(tmp.root)
}
