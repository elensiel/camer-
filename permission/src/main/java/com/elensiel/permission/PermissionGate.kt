package com.elensiel.permission

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.text.Typography.bullet

// ---------------------------------------------------------------
// Gate stage
// ---------------------------------------------------------------

private sealed class GateStage {
    data object Checking : GateStage()
    data object Granted : GateStage()
    data class Rationale(val permissions: List<PermissionData>) : GateStage()
    data class PermanentlyDenied(val permissions: List<PermissionData>) : GateStage()
}

// ---------------------------------------------------------------
// Context helper
// ---------------------------------------------------------------

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// ---------------------------------------------------------------
// PermissionGate
// ---------------------------------------------------------------

@Composable
fun PermissionGate(
    permissions: List<PermissionData>,
    content: @Composable () -> Unit,
) {

    // -- State --

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = remember(context) { context.findActivity() }
    val prefs = remember { PermissionPreferences(context) }

    var stage by remember { mutableStateOf<GateStage>(GateStage.Checking) }

    // -- Evaluation --

    fun evaluate() {
        val ungranted = permissions.filter {
            ContextCompat.checkSelfPermission(context, it.permission) !=
                    PackageManager.PERMISSION_GRANTED
        }

        val ungrantedRequired = ungranted.filter { it.isRequired }

        if (ungrantedRequired.isEmpty()) {
            stage = GateStage.Granted
            return
        }

        val permanentlyDenied = ungrantedRequired.filter { p ->
            prefs.hasRequestedBefore(p.permission) &&
                    activity?.shouldShowRequestPermissionRationale(p.permission) == false
        }

        stage = if (permanentlyDenied.isNotEmpty()) {
            GateStage.PermanentlyDenied(permanentlyDenied)
        } else {
            GateStage.Rationale(ungranted)
        }
    }

    // -- Lifecycle --

    LaunchedEffect(Unit) { evaluate() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) evaluate()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissions.forEach { prefs.markRequested(it.permission) }
        evaluate()
    }

    // -- UI --

    when (val currentStage = stage) {
        is GateStage.Checking -> Unit
        is GateStage.Granted -> content()

        is GateStage.Rationale -> RationaleDialog(
            permissions = currentStage.permissions,
            onContinue = {
                launcher.launch(currentStage.permissions.map { it.permission }.toTypedArray())
            }
        )

        is GateStage.PermanentlyDenied -> SettingsDialog(
            permissions = currentStage.permissions,
            onOpenSettings = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    )
                )
            }
        )
    }
}

// ---------------------------------------------------------------
// Dialogs
// ---------------------------------------------------------------

@Composable
private fun RationaleDialog(
    permissions: List<PermissionData>,
    onContinue: () -> Unit,
) {
    val required = permissions.filter { it.isRequired }
    val optional = permissions.filter { !it.isRequired }

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Permissions required") },
        text = {
            Text(
                buildString {
                    if (required.isNotEmpty()) {
                        append("Required for the app to work correctly:\n\n")
                        required.forEach { append("$bullet ${it.displayName} — ${it.rationale}\n") }
                    }
                    if (optional.isNotEmpty()) {
                        append("Optional, enables extra features:\n\n")
                        optional.forEach { append("$bullet ${it.displayName} — ${it.rationale}\n") }
                    }
                }
            )
        },
        confirmButton = {
            Button(onContinue) { Text("Continue") }
        },
    )
}

@Composable
private fun SettingsDialog(
    permissions: List<PermissionData>,
    onOpenSettings: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Permissions needed") },
        text = {
            Text(
                buildString {
                    append("The following permissions were denied and must be enabled from Settings:\n\n")
                    permissions.forEach { p ->
                        append("$bullet ${p.displayName}\n")
                    }
                }
            )
        },
        confirmButton = {
            Button(onOpenSettings) { Text("Open Settings") }
        },
    )
}
