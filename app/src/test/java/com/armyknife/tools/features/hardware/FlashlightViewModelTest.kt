package com.armyknife.tools.features.hardware

import org.junit.Assert.*
import org.junit.Test

class FlashlightViewModelTest {

    private val vm = FlashlightViewModel()

    @Test
    fun `initial torch state is off`() {
        assertFalse(vm.isTorchOn)
    }

    @Test
    fun `initial sos mode is off`() {
        assertFalse(vm.isSosMode)
    }

    @Test
    fun `toggle torch turns on when off`() {
        vm.toggleTorch()
        assertTrue(vm.isTorchOn)
    }

    @Test
    fun `toggle torch turns off when on`() {
        vm.toggleTorch()
        vm.toggleTorch()
        assertFalse(vm.isTorchOn)
    }

    @Test
    fun `sos mode starts disabled`() {
        assertFalse(vm.isSosMode)
    }

    @Test
    fun `enable sos mode sets flag`() {
        vm.enableSos()
        assertTrue(vm.isSosMode)
    }

    @Test
    fun `disable sos mode clears flag`() {
        vm.enableSos()
        vm.disableSos()
        assertFalse(vm.isSosMode)
    }

    @Test
    fun `enable sos turns torch off`() {
        vm.toggleTorch()
        assertTrue(vm.isTorchOn)
        vm.enableSos()
        assertFalse(vm.isTorchOn)
    }

    @Test
    fun `disable sos leaves torch state unchanged when on`() {
        vm.toggleTorch()
        vm.enableSos()
        vm.disableSos()
        assertFalse(vm.isTorchOn)
    }

    @Test
    fun `camera id is set and accessible`() {
        vm.setCameraId("camera0")
        assertEquals("camera0", vm.cameraId)
    }

    @Test
    fun `has torch returns false when camera id null`() {
        assertFalse(vm.hasTorch)
    }

    @Test
    fun `set flash state updates torch on value`() {
        vm.toggleTorch()
        assertTrue(vm.isTorchOn)
        vm.setFlashState(false)
        assertFalse(vm.isTorchOn)
    }

    @Test
    fun `sos pattern sequence is correct`() {
        // SOS = ... --- ... (3 short, 3 long, 3 short)
        val pattern = FlashlightViewModel.sosPattern
        assertEquals(9, pattern.size)
    }

    @Test
    fun `sos pattern has short dots first`() {
        val pattern = FlashlightViewModel.sosPattern
        assertEquals(100, pattern[0])  // short dot
        assertEquals(100, pattern[1])
        assertEquals(100, pattern[2])
    }

    @Test
    fun `sos pattern has long dashes middle`() {
        val pattern = FlashlightViewModel.sosPattern
        assertEquals(300, pattern[3])  // long dash
        assertEquals(300, pattern[4])
        assertEquals(300, pattern[5])
    }

    @Test
    fun `sos pattern has short dots last`() {
        val pattern = FlashlightViewModel.sosPattern
        assertEquals(100, pattern[6])  // short dot
        assertEquals(100, pattern[7])
        assertEquals(100, pattern[8])
    }
}
