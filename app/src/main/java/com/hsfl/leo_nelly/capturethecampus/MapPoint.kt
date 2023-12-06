package com.hsfl.leo_nelly.capturethecampus

import android.location.Location

data class MapPoint(
    val latitude: Double,
    val longitude: Double,
    var state: PointState,
    val mapX: Float = 0f,
    val mapY: Float = 0f,
    val location: Location? = null
)

enum class PointState {
    NOT_VISITED, VISITED
}
