package com.armyknife.tools.features.sensor

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SpeedometerViewModelTest {

    private lateinit var vm: SpeedometerViewModel

    @Before
    fun setup() {
        vm = SpeedometerViewModel()
    }

    // --- Speed update from GPS ---

    @Test
    fun `update speed from meters per second`() {
        vm.onSpeedChanged(10f) // 10 m/s
        assertEquals(36f, vm.uiState.currentSpeedKmh, 0.01f)
    }

    @Test
    fun `zero speed`() {
        vm.onSpeedChanged(0f)
        assertEquals(0f, vm.uiState.currentSpeedKmh, 0.01f)
    }

    @Test
    fun `negative speed is clamped to zero`() {
        vm.onSpeedChanged(-5f)
        assertEquals(0f, vm.uiState.currentSpeedKmh, 0.01f)
    }

    @Test
    fun `high speed conversion`() {
        vm.onSpeedChanged(27.78f) // 100 km/h
        assertEquals(100f, vm.uiState.currentSpeedKmh, 0.01f)
    }

    // --- Unit conversion ---

    @Test
    fun `mph conversion from kmh`() {
        vm.onSpeedChanged(44.704f) // 160.334 km/h ≈ 100 mph
        val mph = vm.toMph(vm.uiState.currentSpeedKmh)
        assertEquals(100f, mph, 0.5f)
    }

    @Test
    fun `knots conversion from kmh`() {
        vm.onSpeedChanged(51.4444f) // 185.2 km/h = 100 knots
        val knots = vm.toKnots(vm.uiState.currentSpeedKmh)
        assertEquals(100f, knots, 0.5f)
    }

    @Test
    fun `display speed respects selected unit`() {
        vm.onSpeedChanged(10f) // 36 km/h

        assertEquals(36f, vm.uiState.displaySpeed(SpeedUnit.KMH), 0.01f)

        val mph = vm.uiState.displaySpeed(SpeedUnit.MPH)
        assertEquals(22.37f, mph, 0.1f)

        val knots = vm.uiState.displaySpeed(SpeedUnit.KNOTS)
        assertEquals(19.44f, knots, 0.1f)
    }

    @Test
    fun `unit label returns correct string`() {
        assertEquals("km/h", SpeedUnit.KMH.label)
        assertEquals("mph", SpeedUnit.MPH.label)
        assertEquals("knots", SpeedUnit.KNOTS.label)
    }

    // --- Max speed tracking ---

    @Test
    fun `max speed tracks highest value`() {
        vm.onSpeedChanged(10f) // 36 km/h
        vm.onSpeedChanged(15f) // 54 km/h
        vm.onSpeedChanged(8f)  // 28.8 km/h
        assertEquals(54f, vm.uiState.maxSpeedKmh, 0.01f)
    }

    @Test
    fun `max speed is zero initially`() {
        assertEquals(0f, vm.uiState.maxSpeedKmh, 0.01f)
    }

    @Test
    fun `reset clears current and max speed`() {
        vm.onSpeedChanged(20f)
        vm.reset()
        assertEquals(0f, vm.uiState.currentSpeedKmh, 0.01f)
        assertEquals(0f, vm.uiState.maxSpeedKmh, 0.01f)
    }

    // --- Unit switching ---

    @Test
    fun `switch unit updates selected unit`() {
        assertEquals(SpeedUnit.KMH, vm.uiState.selectedUnit)
        vm.switchUnit(SpeedUnit.MPH)
        assertEquals(SpeedUnit.MPH, vm.uiState.selectedUnit)
        vm.switchUnit(SpeedUnit.KNOTS)
        assertEquals(SpeedUnit.KNOTS, vm.uiState.selectedUnit)
    }

    // --- GPS accuracy ---

    @Test
    fun `gps accuracy update`() {
        vm.onAccuracyChanged(10f)
        assertEquals(10f, vm.uiState.accuracy, 0.01f)
    }

    @Test
    fun `is moving returns true when speed above threshold`() {
        vm.onSpeedChanged(1f) // 3.6 km/h
        assertTrue(vm.uiState.isMoving)
    }

    @Test
    fun `is moving returns false when stationary`() {
        vm.onSpeedChanged(0f)
        assertFalse(vm.uiState.isMoving)
    }

    @Test
    fun `is moving threshold is around half kmh`() {
        vm.onSpeedChanged(0.12f) // 0.43 km/h — below threshold
        assertFalse(vm.uiState.isMoving)

        vm.onSpeedChanged(0.16f) // 0.57 km/h — above threshold
        assertTrue(vm.uiState.isMoving)
    }
}
