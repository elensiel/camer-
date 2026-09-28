package com.elensiel.camer_.processors.video

import java.io.File

fun interface VideoProcessor {
    suspend fun process(input: File): File
}
