package com.elensiel.camer_.data

import java.io.File

private val photoExtensions = setOf("jpg", "png", "webp", "dng")

fun purgeStaleCache(dir: File) {
    dir.listFiles { f ->
        f.isFile && f.extension.lowercase() in photoExtensions
    }?.forEach { it.delete() }
}
