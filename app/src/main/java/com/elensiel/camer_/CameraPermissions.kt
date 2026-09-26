package com.elensiel.camer_

import android.Manifest
import com.elensiel.permission.PermissionData

object CameraPermissions {
    val permissions = listOf(
        PermissionData(
            permission = Manifest.permission.CAMERA,
            displayName = "Camera",
            rationale = "Needed to take photos with the app.",
        )
    )
}
