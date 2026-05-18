package com.armyknife.tools.features.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Recording state machine states.
 */
enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED
}

/**
 * ViewModel for the Voice Recorder tool.
 * Manages recording lifecycle (start/pause/resume/stop), duration tracking,
 * volume level, sample rate, and file naming.
 */
class VoiceRecorderViewModel : ViewModel() {

    var state: RecordingState = RecordingState.IDLE
        private set

    var durationMs: Long by mutableLongStateOf(0L)
        private set

    var volumeLevel: Int by mutableIntStateOf(0)
        private set

    var sampleRate: Int = 44100
        private set

    companion object {
        /**
         * Format duration in milliseconds to MM:SS string.
         */
        fun formatDuration(ms: Long): String {
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

        /**
         * Estimate file size in bytes for a given duration.
         * Rough estimate: ~16kbps for default AAC encoding.
         */
        fun estimateFileSizeForDuration(durationMs: Long, sampleRate: Int): Long {
            if (durationMs <= 0) return 0L
            // Estimate: sampleRate * 16 bits * 2 channels / 8 = bytes per second
            // Simplified: ~352 bytes/sec per 1000 sample rate unit
            val bytesPerSecond = (sampleRate * 16 * 1 / 8).toLong()
            return (bytesPerSecond * durationMs / 1000)
        }
    }

    // ── Recording lifecycle ──

    fun startRecording() {
        if (state != RecordingState.IDLE) return
        state = RecordingState.RECORDING
        durationMs = 0L
    }

    fun pauseRecording() {
        if (state != RecordingState.RECORDING) return
        state = RecordingState.PAUSED
    }

    fun resumeRecording() {
        if (state != RecordingState.PAUSED) return
        state = RecordingState.RECORDING
    }

    fun stopRecording() {
        if (state == RecordingState.IDLE) return
        state = RecordingState.IDLE
        durationMs = 0L
        volumeLevel = 0
    }

    // ── Duration tick ──

    /**
     * Called periodically to update elapsed duration.
     * Only increments when actively recording (not paused).
     */
    fun tick(deltaMs: Long) {
        if (state == RecordingState.RECORDING) {
            durationMs += deltaMs
        }
    }

    // ── Volume ──

    fun updateVolume(value: Int) {
        volumeLevel = value.coerceIn(0, 100)
    }

    // ── Sample rate ──

    fun cycleSampleRate() {
        sampleRate = when (sampleRate) {
            44100 -> 22050
            22050 -> 11025
            else -> 44100
        }
    }

    // ── File naming ──

    fun generateFilename(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        return "recording_${timestamp}.m4a"
    }

    // ── File size estimation ──

    fun estimateFileSize(durationMs: Long): Long =
        estimateFileSizeForDuration(durationMs, sampleRate)

    // ── Reset ──

    fun reset() {
        state = RecordingState.IDLE
        durationMs = 0L
        volumeLevel = 0
        sampleRate = 44100
    }
}
