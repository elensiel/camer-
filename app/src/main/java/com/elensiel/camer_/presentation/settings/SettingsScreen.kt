package com.elensiel.camer_.presentation.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elensiel.camer_.R
import com.elensiel.camer_.presentation.components.SettingsDropdownItem

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(onBack = onBack)

    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Header("Settings", onBack)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                item {
                    SettingsDropdownItem(
                        title = "Image Format",
                        options = state.availableImageFormats,
                        selected = state.imageFormat,
                        optionLabel = { it.label },
                        onSelected = viewModel::onImageFormat,
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(
    title: String,
    onBack: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_back),
                contentDescription = "Back",
            )
        }

        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
