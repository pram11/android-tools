package com.armyknife.tools.features.sensor

import org.junit.Assert.assertEquals
import org.junit.Test

class CompassViewModelTest {

    private val vm = CompassViewModel()

    @Test
    fun `bearing from rotation matrix - facing north returns 0 degrees`() {
        // Android SensorManager.getOrientation: az = atan2(R[1], R[5])
        // North (0°): atan2(0, 1) = 0
        val remappedMatrix = floatArrayOf(
            1f, 0f, 0f,   // col 0
            0f, 1f, 0f,   // col 1
            0f, 0f, 1f    // col 2
        )
        assertEquals(0f, vm.calculateBearing(remappedMatrix), 0.1f)
    }

    @Test
    fun `bearing from rotation matrix - facing east returns 90 degrees`() {
        // East (90°): atan2(1, 0) = 90
        // R[1]=1, R[5]=0
        val remappedMatrix = floatArrayOf(
            0f, 1f, 0f,
            -1f, 0f, 0f,
            0f, 0f, 1f
        )
        assertEquals(90f, vm.calculateBearing(remappedMatrix), 0.1f)
    }

    @Test
    fun `bearing from rotation matrix - facing south returns 180 degrees`() {
        // South (180°): atan2(0, -1) = 180
        // Column-major: R[1]=0, R[5]=-1
        // Valid rotation matrix: [-1,0,0] [0,0,-1] [0,-1,0]
        val remappedMatrix = floatArrayOf(
            -1f, 0f, 0f,
            0f, 0f, -1f,
            0f, -1f, 0f
        )
        assertEquals(180f, vm.calculateBearing(remappedMatrix), 0.1f)
    }

    @Test
    fun `bearing from rotation matrix - facing west returns 270 degrees`() {
        // West (270°): atan2(-1, 0) = -90 → 270
        // R[1]=-1, R[5]=0
        val remappedMatrix = floatArrayOf(
            0f, -1f, 0f,
            1f, 0f, 0f,
            0f, 0f, 1f
        )
        assertEquals(270f, vm.calculateBearing(remappedMatrix), 0.1f)
    }

    @Test
    fun `bearing is always clamped to 0-360 range`() {
        val matrices = listOf(
            floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f),
            floatArrayOf(0f, 1f, 0f, -1f, 0f, 0f, 0f, 0f, 1f),
            floatArrayOf(-1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f),
            floatArrayOf(0f, -1f, 0f, 1f, 0f, 0f, 0f, 0f, 1f),
        )
        matrices.forEach { matrix ->
            val bearing = vm.calculateBearing(matrix)
            assert(bearing >= 0f && bearing < 360f) {
                "Bearing $bearing out of range [0, 360)"
            }
        }
    }

    @Test
    fun `direction label maps correctly to cardinal directions`() {
        assertEquals("N", vm.getDirectionLabel(0f))
        assertEquals("N", vm.getDirectionLabel(11f))
        assertEquals("NNE", vm.getDirectionLabel(22.5f))
        assertEquals("NE", vm.getDirectionLabel(45f))
        assertEquals("ENE", vm.getDirectionLabel(67.5f))
        assertEquals("E", vm.getDirectionLabel(90f))
        assertEquals("ESE", vm.getDirectionLabel(112.5f))
        assertEquals("SE", vm.getDirectionLabel(135f))
        assertEquals("SSE", vm.getDirectionLabel(157.5f))
        assertEquals("S", vm.getDirectionLabel(180f))
        assertEquals("SSW", vm.getDirectionLabel(202.5f))
        assertEquals("SW", vm.getDirectionLabel(225f))
        assertEquals("WSW", vm.getDirectionLabel(247.5f))
        assertEquals("W", vm.getDirectionLabel(270f))
        assertEquals("WNW", vm.getDirectionLabel(292.5f))
        assertEquals("NW", vm.getDirectionLabel(315f))
        assertEquals("NNW", vm.getDirectionLabel(337.5f))
        assertEquals("N", vm.getDirectionLabel(359f))
    }

    @Test
    fun `state updates with new bearing`() {
        vm.updateBearing(45f)
        assertEquals(45f, vm.uiState.bearing, 0.1f)
        assertEquals("NE", vm.uiState.direction)
    }
}
