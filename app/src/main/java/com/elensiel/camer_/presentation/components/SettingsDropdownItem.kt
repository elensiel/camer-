package com.elensiel.camer_.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

private val MenuVerticalPadding = 8.dp

@Composable
fun <T> SettingsDropdownItem(
    title: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var buttonSize by remember { mutableStateOf(DpSize.Zero) }
    val density = LocalDensity.current

    val ordered = remember(options, selected) {
        listOf(selected) + options.filter { it != selected }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )

        Box {
            TextButton(
                onClick = { isExpanded = true },
                modifier = Modifier.onSizeChanged {
                    buttonSize = with(density) {
                        DpSize(it.width.toDp(), it.height.toDp())
                    }
                },
            ) {
                Text(optionLabel(selected))
            }

            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier.width(buttonSize.width),
                offset = DpOffset(0.dp, -(buttonSize.height + MenuVerticalPadding)),
            ) {
                ordered.forEach { option ->
                    val isSelected = option == selected
                    val colors = MaterialTheme.colorScheme

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = optionLabel(option),
                                color =
                                    if (isSelected) colors.onSecondaryContainer
                                    else colors.onSurface,
                            )
                        },
                        onClick = {
                            onSelected(option)
                            isExpanded = false
                        },
                        modifier = Modifier.background(
                            color =
                                if (isSelected) colors.secondaryContainer
                                else Color.Transparent,
                        ),
                    )
                }
            }
        }
    }
}
