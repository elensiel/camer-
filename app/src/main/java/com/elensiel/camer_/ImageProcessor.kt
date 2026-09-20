package com.elensiel.camer_

import java.io.File

fun interface ImageProcessor {
    suspend fun process(input: File): File
}
