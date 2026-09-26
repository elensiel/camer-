package com.elensiel.permission

import android.content.Context
import androidx.core.content.edit

/**
 * Tracks, per permission string, whether this app has ever asked the user for it.
 *
 * This exists because Android's [android.content.pm.PackageManager] can only tell you
 * whether a permission is currently granted or denied — it can't tell you *why* it's
 * denied. A first-time denial and a "don't ask again" denial look identical from
 * [androidx.core.content.ContextCompat.checkSelfPermission] alone. By remembering
 * whether we've requested a permission before, callers (e.g. [PermissionGate]) can
 * distinguish "never asked yet" from "asked and permanently denied," and route the
 * user to an explanation dialog versus an "open Settings" dialog accordingly.
 */
internal class PermissionPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasRequestedBefore(permission: String): Boolean =
        prefs.getBoolean(key(permission), false)

    fun markRequested(permission: String) {
        prefs.edit { putBoolean(key(permission), true) }
    }

    private fun key(permission: String) = "requested_$permission"

    private companion object {
        const val PREFS_NAME = "permission_gate_prefs"
    }
}
