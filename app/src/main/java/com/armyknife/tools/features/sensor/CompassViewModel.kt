package com.armyknife.tools.features.sensor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * UI state for the Compass screen.
 */
data class CompassUiState(
    val bearing: Float = 0f,
    val direction: String = "N"
)

/**
 * ViewModel for the Compass tool.
 * Handles bearing calculation from rotation matrix and direction labeling.
 */
class CompassViewModel : ViewModel() {

    var uiState by mutableStateOf(CompassUiState())
        private set

    /**
     * Calculate bearing (heading in degrees [0, 360)) from a remapped rotation matrix.
     * Matches Android SensorManager.getOrientation:
     *   azimuth = atan2(R[1], R[5])
     * where R is the remapped rotation matrix in column-major order.
     */
    fun calculateBearing(remappedMatrix: FloatArray): Float {
        // Android SensorManager.getOrientation: az = atan2(R[1], R[5])
        val azimuth = Math.toDegrees(Math.atan2(remappedMatrix[1].toDouble(), remappedMatrix[5].toDouble())).toFloat()
        return azimuth.mod(360f)
    }

    /**
     * Map a bearing (0-360) to a cardinal direction label.
     * Uses 16-point compass rose with 22.5-degree increments.
     */
    fun getDirectionLabel(bearing: Float): String {
        val directions = arrayOf(
            "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
        )
        val index = ((bearing + 11.25f) / 22.5f).toInt() % 16
        return directions[index]
    }

    /**
     * Update the bearing and direction in the UI state.
     */
    fun updateBearing(bearing: Float) {
        val clampedBearing = bearing.mod(360f)
        uiState = uiState.copy(
            bearing = clampedBearing,
            direction = getDirectionLabel(clampedBearing)
        )
    }
}

/**
 * Float extension for modulo that always returns positive result.
 */
private fun Float.mod(divisor: Float): Float {
    var result = this % divisor
    if (result < 0) result += divisor
    return result
}
