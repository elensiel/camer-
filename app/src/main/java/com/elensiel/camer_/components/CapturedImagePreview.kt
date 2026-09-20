package com.elensiel.camer_.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun CapturedImagePreview(
    modifier: Modifier = Modifier,
    photoFile: File,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    Box(
        modifier = modifier
    ) {
        AsyncImage(
            model = photoFile,
            contentDescription = "Captured photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Button(
                onClick = onDiscard,
            ) {
                Text("Discard")
            }

            Button(
                onClick = onSave,
            ) {
                Text("Save")
            }
        }
    }
}
