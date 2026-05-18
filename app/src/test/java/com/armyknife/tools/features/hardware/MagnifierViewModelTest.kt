package com.armyknife.tools.features.hardware

import org.junit.Assert.*
import org.junit.Test

class MagnifierViewModelTest {

    private val vm = MagnifierViewModel()

    @Test
    fun `initial zoom ratio is one`() {
        assertEquals(1.0f, vm.zoomRatio, 0.001f)
    }

    @Test
    fun `set zoom ratio clamps to min one`() {
        vm.setZoomRatio(0.5f)
        assertEquals(1.0f, vm.zoomRatio, 0.001f)
    }

    @Test
    fun `set zoom ratio clamps to max ten`() {
        vm.setZoomRatio(15.0f)
        assertEquals(10.0f, vm.zoomRatio, 0.001f)
    }

    @Test
    fun `set zoom ratio accepts valid value`() {
        vm.setZoomRatio(5.0f)
        assertEquals(5.0f, vm.zoomRatio, 0.001f)
    }

    @Test
    fun `zoom level string shows 1x at min`() {
        assertEquals("1x", vm.zoomLevelString)
    }

    @Test
    fun `zoom level string shows correct multiplier`() {
        vm.setZoomRatio(3.5f)
        assertEquals("3.5x", vm.zoomLevelString)
    }

    @Test
    fun `zoom level string shows whole number at integer`() {
        vm.setZoomRatio(4.0f)
        assertEquals("4x", vm.zoomLevelString)
    }

    @Test
    fun `camera facing starts as rear`() {
        assertEquals(CameraFacing.REAR, vm.cameraFacing)
    }

    @Test
    fun `flip camera toggles facing`() {
        assertEquals(CameraFacing.REAR, vm.cameraFacing)
        vm.flipCamera()
        assertEquals(CameraFacing.FRONT, vm.cameraFacing)
        vm.flipCamera()
        assertEquals(CameraFacing.REAR, vm.cameraFacing)
    }

    @Test
    fun `reset zoom returns to one`() {
        vm.setZoomRatio(5.0f)
        vm.resetZoom()
        assertEquals(1.0f, vm.zoomRatio, 0.001f)
    }

    @Test
    fun `max zoom ratio is accessible`() {
        vm.maxZoomRatio = 5.0f
        assertEquals(5.0f, vm.maxZoomRatio, 0.001f)
    }

    @Test
    fun `set zoom ratio clamps to current max zoom`() {
        vm.maxZoomRatio = 3.0f
        vm.setZoomRatio(5.0f)
        assertEquals(3.0f, vm.zoomRatio, 0.001f)
    }
}
