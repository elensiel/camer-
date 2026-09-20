package com.tomatorangers.tomaito.permission

import android.Manifest

object AppPermissions {
    val required: List<AppPermission>
        get() = buildList {
            add(
                AppPermission(
                    permission = Manifest.permission.CAMERA,
                    displayName = "Camera",
                )
            )
        }
}
