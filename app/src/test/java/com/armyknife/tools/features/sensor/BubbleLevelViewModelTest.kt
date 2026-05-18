package com.armyknife.tools.features.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BubbleLevelViewModelTest {

    private val vm = BubbleLevelViewModel()

    @Test
    fun `flat surface - zero tilt returns 0 0`() {
        // Device lying perfectly flat, gravity only on Z axis
        vm.onAccelerometerChanged(0f, 0f, 9.81f)
        assertEquals(0f, vm.uiState.tiltX, 0.5f)
        assertEquals(0f, vm.uiState.tiltY, 0.5f)
    }

    @Test
    fun `tilted along X axis - positive pitch`() {
        // Tilt forward along X axis
        vm.onAccelerometerChanged(4.9f, 0f, 8.49f)
        assertTrue(vm.uiState.tiltX > 0f)
        assertEquals(0f, vm.uiState.tiltY, 0.5f)
    }

    @Test
    fun `tilted along Y axis - positive roll`() {
        // Tilt right along Y axis
        vm.onAccelerometerChanged(0f, 4.9f, 8.49f)
        assertTrue(vm.uiState.tiltY > 0f)
        assertEquals(0f, vm.uiState.tiltX, 0.5f)
    }

    @Test
    fun `negative X tilt`() {
        // Tilt backward along X axis
        vm.onAccelerometerChanged(-4.9f, 0f, 8.49f)
        assertTrue(vm.uiState.tiltX < 0f)
    }

    @Test
    fun `negative Y tilt`() {
        // Tilt left along Y axis
        vm.onAccelerometerChanged(0f, -4.9f, 8.49f)
        assertTrue(vm.uiState.tiltY < 0f)
    }

    @Test
    fun `tilt angles clamped to -90 to 90`() {
        // Extreme tilt — vertical along X
        vm.onAccelerometerChanged(9.81f, 0f, 0f)
        assertTrue(vm.uiState.tiltX <= 90f && vm.uiState.tiltX >= -90f)

        // Extreme tilt — vertical along Y
        vm.onAccelerometerChanged(0f, 9.81f, 0f)
        assertTrue(vm.uiState.tiltY <= 90f && vm.uiState.tiltY >= -90f)
    }

    @Test
    fun `isLevel returns true when flat`() {
        vm.onAccelerometerChanged(0f, 0f, 9.81f)
        assertTrue(vm.uiState.isLevel)
    }

    @Test
    fun `isLevel returns false when tilted beyond threshold`() {
        vm.onAccelerometerChanged(5f, 0f, 8f)
        assertFalse(vm.uiState.isLevel)
    }

    @Test
    fun `isLevel returns true within tolerance threshold`() {
        // Very slight tilt — should still be considered level
        vm.onAccelerometerChanged(0.1f, 0.1f, 9.81f)
        assertTrue(vm.uiState.isLevel)
    }

    @Test
    fun `bubble position calculated from tilt`() {
        vm.onAccelerometerChanged(3.0f, 4.0f, 8.5f)
        // Bubble position should reflect tilt — away from center
        val px = vm.uiState.bubbleX
        val py = vm.uiState.bubbleY
        assertTrue(px > 0f && px < 1f)
        assertTrue(py > 0f && py < 1f)
    }

    @Test
    fun `bubble centered when flat`() {
        vm.onAccelerometerChanged(0f, 0f, 9.81f)
        assertEquals(0.5f, vm.uiState.bubbleX, 0.05f)
        assertEquals(0.5f, vm.uiState.bubbleY, 0.05f)
    }

    @Test
    fun `gravity normalization handles zero gravity gracefully`() {
        vm.onAccelerometerChanged(0f, 0f, 0f)
        // Should not throw, defaults to flat
        assertEquals(0f, vm.uiState.tiltX, 0.5f)
        assertEquals(0f, vm.uiState.tiltY, 0.5f)
    }
}
