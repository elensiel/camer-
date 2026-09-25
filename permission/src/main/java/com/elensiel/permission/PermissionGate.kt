package com.elensiel.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlin.text.Typography.bullet

@Composable
fun PermissionGate(
    onPermissionGranted: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val permissions = remember { AppPermissionList.required }

    // ---------------------------------------------------------------
    // State
    // ---------------------------------------------------------------

    var permissionsGranted by remember { mutableStateOf(false) }
    var showExplanationDialog by remember { mutableStateOf(false) }
    var showDeniedDialog by remember { mutableStateOf(false) }

    // ---------------------------------------------------------------
    // Permission logic
    // ---------------------------------------------------------------

    fun areAllPermissionsGranted(): Boolean {
        return permissions.all { requiredPermission ->
            ContextCompat.checkSelfPermission(
                context,
                requiredPermission.permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // handles the aftermath of permission request(s)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (areAllPermissionsGranted()) {
            permissionsGranted = true
            showDeniedDialog = false
        } else {
            showDeniedDialog = true
        }
    }

    fun requestPermissions() {
        showExplanationDialog = false

        permissionLauncher.launch(
            permissions
                .map { it.permission }
                .toTypedArray()
        )
    }

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    LaunchedEffect(Unit) {
        if (areAllPermissionsGranted()) {
            permissionsGranted = true
        } else {
            showExplanationDialog = true
        }
    }

    // ---------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------

    when {
        permissionsGranted -> {
            onPermissionGranted()
        }

        showExplanationDialog -> {
            ExplanationDialog(
                permissions = permissions,
                onContinue = {
                    showExplanationDialog = false
                    requestPermissions()
                }
            )
        }

        showDeniedDialog -> {
            DeniedDialog(
                onTryAgain = { requestPermissions() },
                onExit = { (context as? Activity)?.finishAndRemoveTask() }
            )
        }
    }
}

@Composable
private fun ExplanationDialog(
    permissions: List<AppPermissionData>,
    onContinue: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Permissions required") },
        text = {
            Text(
                buildString {
                    append("The following permissions are required for the application to operate correctly.\n\n")

                    permissions.forEach { permission ->
                        append("$bullet ${permission.displayName}\n")
                    }
                }
            )
        },
        confirmButton = {
            Button(onClick = onContinue) {
                Text("Continue")
            }
        }
    )
}

@Composable
private fun DeniedDialog(
    onTryAgain: () -> Unit,
    onExit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Cannot continue") },
        text = {
            Text(
                "The required permissions are required for the application to operate correctly.\n\n" +
                        "You can try granting the permissions again."
            )
        },
        confirmButton = {
            Button(onClick = onTryAgain) {
                Text("Try again")
            }
        },
        dismissButton = {
            Button(onClick = onExit) {
                Text("Exit")
            }
        }
    )
}
