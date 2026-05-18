package com.armyknife.tools.features.sensor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Supported speed display units.
 */
enum class SpeedUnit(val label: String) {
    KMH("km/h"),
    MPH("mph"),
    KNOTS("knots")
}

/**
 * UI state for the Speedometer screen.
 */
data class SpeedometerUiState(
    val currentSpeedKmh: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val selectedUnit: SpeedUnit = SpeedUnit.KMH,
    val accuracy: Float = -1f,
    val isMoving: Boolean = false,
    val unitThreshold: Float = 0.5f // km/h — considered "moving" above this
) {
    /**
     * Display speed converted to the selected unit.
     */
    fun displaySpeed(unit: SpeedUnit): Float = when (unit) {
        SpeedUnit.KMH -> currentSpeedKmh
        SpeedUnit.MPH -> currentSpeedKmh / 1.60934f
        SpeedUnit.KNOTS -> currentSpeedKmh / 1.852f
    }
}

/**
 * ViewModel for the Speedometer tool.
 * Converts GPS speed (m/s) to display units, tracks max speed.
 */
class SpeedometerViewModel : ViewModel() {

    var uiState by mutableStateOf(SpeedometerUiState())
        private set

    /**
     * Called with GPS-provided speed in meters per second.
     * Converts to km/h and tracks max speed.
     */
    fun onSpeedChanged(speedMs: Float) {
        val speedKmh = maxOf(0f, speedMs) * 3.6f
        val newMax = maxOf(uiState.maxSpeedKmh, speedKmh)
        uiState = uiState.copy(
            currentSpeedKmh = speedKmh,
            maxSpeedKmh = newMax,
            isMoving = speedKmh > uiState.unitThreshold
        )
    }

    /**
     * Called with GPS accuracy in meters.
     */
    fun onAccuracyChanged(accuracy: Float) {
        uiState = uiState.copy(accuracy = accuracy)
    }

    /**
     * Convert km/h to mph.
     */
    fun toMph(kmh: Float): Float = kmh / 1.60934f

    /**
     * Convert km/h to knots.
     */
    fun toKnots(kmh: Float): Float = kmh / 1.852f

    /**
     * Switch the selected display unit.
     */
    fun switchUnit(unit: SpeedUnit) {
        uiState = uiState.copy(selectedUnit = unit)
    }

    /**
     * Reset current speed and max speed to zero.
     */
    fun reset() {
        uiState = uiState.copy(
            currentSpeedKmh = 0f,
            maxSpeedKmh = 0f,
            isMoving = false
        )
    }
}
