package com.armyknife.tools.features.sensor

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI state for Sound Meter.
 */
data class SoundMeterUiState(
    val currentDb: Float = 0f,
    val peakDb: Float = 0f,
    val isRecording: Boolean = false,
    val levelCategory: String = "Silent",
    val rawAmplitude: Double = 0.0,
    val history: List<Float> = emptyList()
)

/**
 * ViewModel for Sound Meter.
 * Reads PCM audio via AudioRecord, computes RMS amplitude → dB SPL approximation.
 * Manages its own coroutine for the audio loop.
 */
class SoundMeterViewModel : ViewModel() {

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val _uiState = MutableStateFlow(SoundMeterUiState())
    val uiState: StateFlow<SoundMeterUiState> = _uiState.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isRunning = false

    /**
     * Check if microphone is available.
     */
    fun isAudioInputAvailable(): Boolean {
        return try {
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            bufferSize != AudioRecord.ERROR_BAD_VALUE
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Start capturing audio amplitude.
     */
    fun startRecording() {
        if (isRunning) return
        if (!isAudioInputAvailable()) return

        isRunning = true
        _uiState.value = _uiState.value.copy(isRecording = true, peakDb = 0f)

        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val buffer = ShortArray(bufferSize)

        audioRecord = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            ).also { it.startRecording() }
        } catch (e: Exception) {
            stopRecording()
            return
        }

        recordingJob = CoroutineScope(Dispatchers.Default).launch {
            val history = mutableListOf<Float>()
            val maxHistorySize = 100

            while (isActive && isRunning) {
                val read = withContext(Dispatchers.Default) {
                    audioRecord?.read(buffer, 0, buffer.size) ?: 0
                }
                if (read > 0) {
                    // Compute RMS
                    var sum = 0.0
                    var maxAbs = 0.0
                    for (i in 0 until read) {
                        val sample = buffer[i].toDouble()
                        sum += sample * sample
                        val abs = kotlin.math.abs(buffer[i].toDouble())
                        if (abs > maxAbs) maxAbs = abs
                    }
                    val rms = kotlin.math.sqrt(sum / read)
                    val db = if (rms > 0) 20 * kotlin.math.log10(rms / 32768.0).toFloat() else -120f
                    val dbClamped = if (db < -120) -120f else db

                    // Peak tracking (smoothed decay)
                    val newPeak = if (dbClamped > _uiState.value.peakDb) dbClamped else _uiState.value.peakDb * 0.995f

                    // Category
                    val category = when {
                        dbClamped < -60f -> "Silent"
                        dbClamped < -40f -> "Very Quiet"
                        dbClamped < -20f -> "Quiet"
                        dbClamped < 0f -> "Moderate"
                        dbClamped < 10f -> "Loud"
                        dbClamped < 20f -> "Very Loud"
                        else -> "Dangerous"
                    }

                    // Update history
                    history.add(dbClamped)
                    if (history.size > maxHistorySize) history.removeAt(0)

                    _uiState.value = _uiState.value.copy(
                        currentDb = dbClamped,
                        peakDb = newPeak,
                        levelCategory = category,
                        rawAmplitude = maxAbs,
                        history = history.toList()
                    )
                }
                delay(50) // ~20 fps update
            }
        }
    }

    /**
     * Stop capturing audio.
     */
    fun stopRecording() {
        isRunning = false
        recordingJob?.cancel()
        recordingJob = null
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        _uiState.value = _uiState.value.copy(isRecording = false)
    }

    /**
     * Reset peak hold.
     */
    fun resetPeak() {
        _uiState.value = _uiState.value.copy(peakDb = 0f)
    }

    override fun onCleared() {
        stopRecording()
    }
}
