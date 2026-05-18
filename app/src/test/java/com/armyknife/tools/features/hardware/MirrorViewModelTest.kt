package com.armyknife.tools.features.hardware

import org.junit.Assert.*
import org.junit.Test

class MirrorViewModelTest {

    private val vm = MirrorViewModel()

    @Test
    fun `initial flip horizontal is true for mirror effect`() {
        assertTrue(vm.flipHorizontal)
    }

    @Test
    fun `toggle flip horizontal changes state`() {
        assertTrue(vm.flipHorizontal)
        vm.toggleFlipHorizontal()
        assertFalse(vm.flipHorizontal)
        vm.toggleFlipHorizontal()
        assertTrue(vm.flipHorizontal)
    }

    @Test
    fun `camera facing is front by default`() {
        assertEquals(CameraFacing.FRONT, vm.cameraFacing)
    }
}
