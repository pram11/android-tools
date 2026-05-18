package com.armyknife.tools.features.sensor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.sqrt

/**
 * UI state for Metal Detector.
 */
data class MetalDetectorUiState(
    val fieldIntensity: Float = 0f,
    val deviation: Float = 0f,
    val baseline: Float = 0f,
    val isCalibrated: Boolean = false,
    val alert: AlertLevel = AlertLevel.NONE,
    val history: List<Float> = emptyList(),
    val peakIntensity: Float = 0f,
    val isDetecting: Boolean = false
)

/**
 * Alert levels for metal detection.
 */
enum class AlertLevel {
    NONE, WEAK, MODERATE, STRONG, EXTREME
}

/**
 * ViewModel for Metal Detector.
 * Processes geomagnetic field data (Sensor.TYPE_MAGNETIC_FIELD) to detect anomalies
 * indicating ferrous metal proximity.
 */
class MetalDetectorViewModel : ViewModel() {

    var uiState by mutableStateOf(MetalDetectorUiState())
        private set

    private val maxHistory = 100
    private val historyBuffer = mutableListOf<Float>()

    // Calibration buffer
    private var calibrationSum = 0f
    private var calibrationCount = 0
    private val calibrationSamples = 30

    private var isCalibrating = false

    /**
     * Start calibration — collects samples to establish baseline field.
     */
    fun startCalibration() {
        calibrationSum = 0f
        calibrationCount = 0
        isCalibrating = true
        uiState = uiState.copy(
            isCalibrated = false,
            baseline = 0f,
            deviation = 0f,
            alert = AlertLevel.NONE,
            isDetecting = false
        )
    }

    /**
     * Process raw magnetic field vector (x, y, z in µT).
     */
    fun onMagneticFieldChanged(x: Float, y: Float, z: Float) {
        val intensity = sqrt(x * x + y * y + z * z)

        if (isCalibrating) {
            calibrationSum += intensity
            calibrationCount++
            if (calibrationCount >= calibrationSamples) {
                val baseline = calibrationSum / calibrationSamples
                isCalibrating = false
                uiState = uiState.copy(
                    isCalibrated = true,
                    baseline = baseline,
                    fieldIntensity = intensity,
                    deviation = intensity - baseline
                )
                return
            }
            // Show calibration progress
            uiState = uiState.copy(fieldIntensity = intensity)
            return
        }

        val baseline = if (uiState.baseline > 0) uiState.baseline else intensity
        val deviation = intensity - uiState.baseline
        val alert = classifyAlert(deviation)
        val isDetecting = alert != AlertLevel.NONE

        historyBuffer.add(deviation)
        if (historyBuffer.size > maxHistory) historyBuffer.removeAt(0)

        val newPeak = if (intensity > uiState.peakIntensity) intensity else uiState.peakIntensity

        uiState = uiState.copy(
            fieldIntensity = intensity,
            deviation = deviation,
            alert = alert,
            isDetecting = isDetecting,
            peakIntensity = newPeak,
            history = historyBuffer.toList()
        )
    }

    fun resetPeak() {
        uiState = uiState.copy(peakIntensity = 0f)
    }

    private fun classifyAlert(deviation: Float): AlertLevel {
        val absDev = kotlin.math.abs(deviation)
        val pctDev = if (uiState.baseline > 0) absDev / uiState.baseline else 0f

        return when {
            pctDev < 0.05f -> AlertLevel.NONE
            pctDev < 0.15f -> AlertLevel.WEAK
            pctDev < 0.30f -> AlertLevel.MODERATE
            pctDev < 0.60f -> AlertLevel.STRONG
            else -> AlertLevel.EXTREME
        }
    }
}
