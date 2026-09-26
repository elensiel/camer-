package com.elensiel.permission

/**
 * Describes a single runtime permission this app needs.
 *
 * @param permission the manifest permission string (e.g. Manifest.permission.CAMERA)
 * @param displayName short human-readable label shown in dialogs
 * @param rationale why the app needs it, shown before requesting and if denied
 * @param isRequired if true, this permission blocks [PermissionGate]'s content
 *   and triggers the "open Settings" dialog.
 */
data class PermissionData(
    val permission: String,
    val displayName: String,
    val rationale: String,
    val isRequired: Boolean = true,
)
