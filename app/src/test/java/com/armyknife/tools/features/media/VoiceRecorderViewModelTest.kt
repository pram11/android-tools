package com.armyknife.tools.features.media

import org.junit.Assert.*
import org.junit.Test

class VoiceRecorderViewModelTest {

    private val vm = VoiceRecorderViewModel()

    // ── Recording State ──

    @Test
    fun `default state is idle`() {
        assertEquals(RecordingState.IDLE, vm.state)
    }

    @Test
    fun `startRecording sets state to recording`() {
        vm.startRecording()
        assertEquals(RecordingState.RECORDING, vm.state)
    }

    @Test
    fun `pauseRecording from recording sets state to paused`() {
        vm.startRecording()
        vm.pauseRecording()
        assertEquals(RecordingState.PAUSED, vm.state)
    }

    @Test
    fun `resumeRecording from paused sets state to recording`() {
        vm.startRecording()
        vm.pauseRecording()
        vm.resumeRecording()
        assertEquals(RecordingState.RECORDING, vm.state)
    }

    @Test
    fun `stopRecording sets state to idle`() {
        vm.startRecording()
        vm.stopRecording()
        assertEquals(RecordingState.IDLE, vm.state)
    }

    @Test
    fun `cannot pause when idle`() {
        vm.pauseRecording()
        assertEquals(RecordingState.IDLE, vm.state)
    }

    @Test
    fun `cannot resume when idle`() {
        vm.resumeRecording()
        assertEquals(RecordingState.IDLE, vm.state)
    }

    @Test
    fun `cannot stop when idle`() {
        vm.stopRecording()
        assertEquals(RecordingState.IDLE, vm.state)
    }

    @Test
    fun `cannot start when already recording`() {
        vm.startRecording()
        vm.startRecording()
        assertEquals(RecordingState.RECORDING, vm.state)
    }

    // ── Duration ──

    @Test
    fun `default duration is zero`() {
        assertEquals(0L, vm.durationMs)
    }

    @Test
    fun `tick increments duration when recording`() {
        vm.startRecording()
        vm.tick(1000L) // 1 second
        assertEquals(1000L, vm.durationMs)
    }

    @Test
    fun `tick does not increment when paused`() {
        vm.startRecording()
        vm.tick(1000L)
        vm.pauseRecording()
        vm.tick(1000L)
        assertEquals(1000L, vm.durationMs)
    }

    @Test
    fun `tick accumulates over multiple calls`() {
        vm.startRecording()
        vm.tick(1000L)
        vm.tick(2000L)
        vm.tick(3000L)
        assertEquals(6000L, vm.durationMs)
    }

    @Test
    fun `tick resets after stop`() {
        vm.startRecording()
        vm.tick(5000L)
        vm.stopRecording()
        assertEquals(0L, vm.durationMs)
    }

    @Test
    fun `tick after resume continues from paused duration`() {
        vm.startRecording()
        vm.tick(3000L)
        vm.pauseRecording()
        vm.tick(2000L) // should not increment
        assertEquals(3000L, vm.durationMs)
        vm.resumeRecording()
        vm.tick(2000L)
        assertEquals(5000L, vm.durationMs)
    }

    // ── Duration formatting ──

    @Test
    fun `formatDuration shows 00-00 for zero`() {
        assertEquals("00:00", VoiceRecorderViewModel.formatDuration(0L))
    }

    @Test
    fun `formatDuration shows seconds`() {
        assertEquals("00:30", VoiceRecorderViewModel.formatDuration(30000L))
    }

    @Test
    fun `formatDuration shows minutes and seconds`() {
        assertEquals("02:30", VoiceRecorderViewModel.formatDuration(150000L))
    }

    @Test
    fun `formatDuration handles exactly 1 hour`() {
        assertEquals("60:00", VoiceRecorderViewModel.formatDuration(3600000L))
    }

    // ── Volume level ──

    @Test
    fun `default volume is zero`() {
        assertEquals(0, vm.volumeLevel)
    }

    @Test
    fun `updateVolume clamps to 0-100`() {
        vm.updateVolume(-10)
        assertEquals(0, vm.volumeLevel)
        vm.updateVolume(150)
        assertEquals(100, vm.volumeLevel)
        vm.updateVolume(42)
        assertEquals(42, vm.volumeLevel)
    }

    // ── Sample rate ──

    @Test
    fun `default sample rate is 44100`() {
        assertEquals(44100, vm.sampleRate)
    }

    @Test
    fun `cycleSampleRate iterates correctly`() {
        assertEquals(44100, vm.sampleRate)
        vm.cycleSampleRate()
        assertEquals(22050, vm.sampleRate)
        vm.cycleSampleRate()
        assertEquals(11025, vm.sampleRate)
        vm.cycleSampleRate()
        assertEquals(44100, vm.sampleRate)
    }

    // ── File naming ──

    @Test
    fun `generateFilename produces timestamped name`() {
        val name = vm.generateFilename()
        assertTrue(name.endsWith(".m4a") || name.endsWith(".mp4") || name.endsWith(".3gp"))
        assertTrue(name.isNotEmpty())
    }

    // ── Reset ──

    @Test
    fun `reset clears all state`() {
        vm.startRecording()
        vm.tick(5000L)
        vm.updateVolume(80)
        vm.cycleSampleRate()

        vm.reset()

        assertEquals(RecordingState.IDLE, vm.state)
        assertEquals(0L, vm.durationMs)
        assertEquals(0, vm.volumeLevel)
        assertEquals(44100, vm.sampleRate)
    }

    // ── File size estimation ──

    @Test
    fun `estimateFileSize returns positive value`() {
        vm.reset() // sampleRate = 44100
        val size = vm.estimateFileSize(60000L) // 1 minute
        assertTrue(size > 0)
    }

    @Test
    fun `estimateFileSize zero duration returns 0`() {
        assertEquals(0L, vm.estimateFileSize(0L))
    }

    @Test
    fun `estimateFileSize scales with duration`() {
        val small = vm.estimateFileSize(10000L)
        val large = vm.estimateFileSize(60000L)
        assertTrue(large > small)
    }
}
