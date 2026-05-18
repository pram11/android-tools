package com.armyknife.tools.features.hardware

import androidx.lifecycle.ViewModel

/**
 * ViewModel for the Flashlight & SOS tool.
 * Manages torch on/off state, SOS mode, and camera ID.
 */
class FlashlightViewModel : ViewModel() {

    companion object {
        /**
         * SOS pattern in milliseconds: ... --- ...
         * Each value = duration flash is ON. Gaps between = same duration.
         * Short dot = 100ms, Long dash = 300ms
         */
        val sosPattern = intArrayOf(100, 100, 100, 300, 300, 300, 100, 100, 100)
    }

    var isTorchOn: Boolean = false
        private set

    var isSosMode: Boolean = false
        private set

    var cameraId: String? = null
        private set

    val hasTorch: Boolean
        get() = cameraId != null

    /**
     * Toggle torch on/off.
     */
    fun toggleTorch() {
        isTorchOn = !isTorchOn
    }

    /**
     * Set torch state directly (from CameraManager callback).
     */
    fun setFlashState(on: Boolean) {
        isTorchOn = on
    }

    /**
     * Set the camera ID for torch control.
     */
    fun setCameraId(id: String) {
        cameraId = id
    }

    /**
     * Enable SOS mode. Turns torch off first.
     */
    fun enableSos() {
        isTorchOn = false
        isSosMode = true
    }

    /**
     * Disable SOS mode. Does not change torch state.
     */
    fun disableSos() {
        isSosMode = false
    }
}
