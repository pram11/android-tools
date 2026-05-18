package com.armyknife.tools.features.hardware

import androidx.lifecycle.ViewModel

/**
 * ViewModel for the Mirror tool.
 * Manages camera facing (front) and horizontal flip for mirror effect.
 */
class MirrorViewModel : ViewModel() {

    /**
     * Flip horizontal for natural mirror effect (front camera).
     */
    var flipHorizontal: Boolean = true
        private set

    /**
     * Camera facing is always front for mirror mode.
     */
    var cameraFacing: CameraFacing = CameraFacing.FRONT
        private set

    /**
     * Toggle horizontal flip (mirror vs non-mirror).
     */
    fun toggleFlipHorizontal() {
        flipHorizontal = !flipHorizontal
    }
}
