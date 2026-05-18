package com.armyknife.tools.features.sensor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.sqrt

/**
 * UI state for the Bubble Level screen.
 */
data class BubbleLevelUiState(
    val tiltX: Float = 0f,
    val tiltY: Float = 0f,
    val isLevel: Boolean = true,
    val bubbleX: Float = 0.5f,
    val bubbleY: Float = 0.5f
)

/**
 * ViewModel for the Bubble Level tool.
 * Computes tilt angles from accelerometer data and derives bubble position.
 */
class BubbleLevelViewModel : ViewModel() {

    private val LEVEL_TOLERANCE = 2f // degrees

    var uiState by mutableStateOf(BubbleLevelUiState())
        private set

    /**
     * Process accelerometer raw values (x, y, z) and update UI state.
     *
     * Tilt X (pitch)  = atan2(ax, sqrt(ay² + az²))
     * Tilt Y (roll)   = atan2(ay, sqrt(ax² + az²))
     *
     * @param ax accelerometer X-axis value
     * @param ay accelerometer Y-axis value
     * @param az accelerometer Z-axis value
     */
    fun onAccelerometerChanged(ax: Float, ay: Float, az: Float) {
        val tiltX = calculateTiltX(ax, ay, az)
        val tiltY = calculateTiltY(ax, ay, az)
        val isLevel = abs(tiltX) <= LEVEL_TOLERANCE && abs(tiltY) <= LEVEL_TOLERANCE
        val bubbleX = tiltToBubblePosition(tiltX)
        val bubbleY = tiltToBubblePosition(tiltY)

        uiState = BubbleLevelUiState(
            tiltX = tiltX,
            tiltY = tiltY,
            isLevel = isLevel,
            bubbleX = bubbleX,
            bubbleY = bubbleY
        )
    }

    private fun calculateTiltX(ax: Float, ay: Float, az: Float): Float {
        val magnitude = sqrt(ay * ay + az * az)
        if (magnitude < 0.001f) return 0f
        return Math.toDegrees(atan2(ax.toDouble(), magnitude.toDouble())).toFloat()
    }

    private fun calculateTiltY(ax: Float, ay: Float, az: Float): Float {
        val magnitude = sqrt(ax * ax + az * az)
        if (magnitude < 0.001f) return 0f
        return Math.toDegrees(atan2(ay.toDouble(), magnitude.toDouble())).toFloat()
    }

    /**
     * Map tilt angle (-90 to +90) to normalized bubble position (0 to 1).
     * Center (0°) → 0.5, max tilt → edges.
     */
    private fun tiltToBubblePosition(tilt: Float): Float {
        // Clamp tilt to ±45° range for visual mapping (45° is extreme tilt on a level)
        val clampedTilt = max(-45f, min(45f, tilt))
        // Map [-45, 45] → [1.0, 0.0] (bubble falls in direction of tilt)
        return 0.5f + (clampedTilt / 90f)
    }

    private fun min(a: Float, b: Float) = if (a < b) a else b
}
