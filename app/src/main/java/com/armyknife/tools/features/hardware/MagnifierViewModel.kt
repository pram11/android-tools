package com.armyknife.tools.features.hardware

import androidx.lifecycle.ViewModel

/**
 * Camera facing direction for the magnifier.
 */
enum class CameraFacing {
    FRONT, REAR
}

/**
 * ViewModel for the Magnifier tool.
 * Manages zoom ratio, camera facing, and zoom UI state.
 */
class MagnifierViewModel : ViewModel() {

    companion object {
        private const val MIN_ZOOM = 1.0f
        private const val DEFAULT_MAX_ZOOM = 10.0f
    }

    var maxZoomRatio: Float = DEFAULT_MAX_ZOOM
        set(value) {
            field = if (value < MIN_ZOOM) MIN_ZOOM else value
        }

    var zoomRatio: Float = MIN_ZOOM
        private set

    var cameraFacing: CameraFacing = CameraFacing.REAR
        private set

    /**
     * Set zoom ratio, clamped between [MIN_ZOOM] and [maxZoomRatio].
     */
    fun setZoomRatio(ratio: Float) {
        zoomRatio = ratio.coerceIn(MIN_ZOOM, maxZoomRatio)
    }

    /**
     * Reset zoom to minimum (1x).
     */
    fun resetZoom() {
        zoomRatio = MIN_ZOOM
    }

    /**
     * Toggle between front and rear camera.
     */
    fun flipCamera() {
        cameraFacing = if (cameraFacing == CameraFacing.REAR) CameraFacing.FRONT else CameraFacing.REAR
    }

    /**
     * Human-readable zoom level string (e.g. "3.5x" or "4x").
     */
    val zoomLevelString: String
        get() {
            val ratio = zoomRatio
            return if (ratio == ratio.toInt().toFloat()) {
                "${ratio.toInt()}x"
            } else {
                "%.1fx".format(ratio)
            }
        }
}
