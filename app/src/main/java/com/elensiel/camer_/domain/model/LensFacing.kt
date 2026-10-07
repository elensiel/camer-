package com.elensiel.camer_.domain.model

enum class LensFacing {
    BACK, FRONT;

    fun flip() = if (this == BACK) FRONT else BACK
}
