package com.elensiel.camer_.presentation.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AdvancedSettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(onBack = onBack)

    Surface(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
//                TextButton(onClick = onBack) { Text("Back") }
                Text(
                    "Advanced Settings",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(Modifier.height(16.dp))

            OptionGroup(
                title = "Image format",
                options = state.availableImageFormats,
                selected = state.imageFormat,
                label = { it.label },
                onSelect = viewModel::onImageFormat,
            )
        }
    }
}

@Composable
private fun <T> OptionGroup(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium
    )

    options.forEach { option ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    role = Role.RadioButton,
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = option == selected,
                onClick = null
            )
            Spacer(Modifier.width(12.dp))
            Text(label(option))
        }
    }
}
