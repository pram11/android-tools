package com.armyknife.tools.features.sensor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * UI state for Lux Meter.
 */
data class LuxMeterUiState(
    val lux: Float = 0f,
    val lightLevel: String = "Dark",
    val peakLux: Float = 0f,
    val history: List<Float> = emptyList()
)

/**
 * ViewModel for Lux Meter.
 */
class LuxMeterViewModel : ViewModel() {

    var uiState by mutableStateOf(LuxMeterUiState())
        private set

    private val maxHistory = 100
    private val historyBuffer = mutableListOf<Float>()

    /**
     * Process a new lux reading from Sensor.TYPE_LIGHT.
     */
    fun onLuxChanged(lux: Float) {
        if (lux < 0) return

        val level = classifyLight(lux)
        val newPeak = if (lux > uiState.peakLux) lux else uiState.peakLux

        historyBuffer.add(lux)
        if (historyBuffer.size > maxHistory) historyBuffer.removeAt(0)

        uiState = uiState.copy(
            lux = lux,
            lightLevel = level,
            peakLux = newPeak,
            history = historyBuffer.toList()
        )
    }

    fun resetPeak() {
        uiState = uiState.copy(peakLux = 0f)
    }

    private fun classifyLight(lux: Float): String = when {
        lux < 1 -> "Pitch Black"
        lux < 5 -> "Very Dark"
        lux < 20 -> "Dark"
        lux < 50 -> "Dim"
        lux < 200 -> "Low Light"
        lux < 500 -> "Office"
        lux < 1000 -> "Daylight"
        lux < 2500 -> "Overcast"
        lux < 10000 -> "Bright"
        lux < 25000 -> "Direct Sun"
        else -> "Extreme"
    }
}
