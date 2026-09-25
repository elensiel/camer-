package com.elensiel.permission

import android.Manifest

object AppPermissionList {
    val required: List<AppPermissionData>
        get() = buildList {
            add(
                AppPermissionData(
                    permission = Manifest.permission.CAMERA,
                    displayName = "Camera",
                )
            )
        }
}
