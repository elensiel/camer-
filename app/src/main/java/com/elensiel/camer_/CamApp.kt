package com.elensiel.camer_

import android.app.Application
import com.elensiel.camer_.di.AppContainer

class CamApp : Application() {
    val container by lazy { AppContainer(this) }
}
