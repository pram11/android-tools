package com.armyknife.tools.features.utility

import org.junit.Assert.*
import org.junit.Test

class UnitConverterViewModelTest {

    private val vm = UnitConverterViewModel()

    // ── Default state ──

    @Test
    fun `default category is length`() {
        assertEquals(UnitCategory.LENGTH, vm.category)
    }

    @Test
    fun `default input is zero`() {
        assertEquals(0.0, vm.inputValue, 0.001)
    }

    @Test
    fun `from unit index defaults to 0`() {
        assertEquals(0, vm.fromUnitIndex)
    }

    @Test
    fun `to unit index defaults to 1`() {
        assertEquals(1, vm.toUnitIndex)
    }

    // ── Category cycling ──

    @Test
    fun `cycleCategory iterates through all categories`() {
        val categories = listOf(
            UnitCategory.LENGTH,
            UnitCategory.WEIGHT,
            UnitCategory.TEMPERATURE,
            UnitCategory.SPEED,
            UnitCategory.DATA,
            UnitCategory.AREA
        )
        var idx = 0
        repeat(categories.size) {
            assertEquals(categories[idx], vm.category)
            vm.cycleCategory()
            idx++
        }
        assertEquals(UnitCategory.LENGTH, vm.category)
    }

    // ── Length conversions ──

    @Test
    fun `convert meters to kilometers`() {
        vm.setFromUnit(0)  // m
        vm.setToUnit(1)    // km
        vm.updateInput(1000.0)
        assertEquals(1.0, vm.result, 0.001)
    }

    @Test
    fun `convert kilometers to meters`() {
        vm.setFromUnit(1)  // km
        vm.setToUnit(0)    // m
        vm.updateInput(5.0)
        assertEquals(5000.0, vm.result, 0.001)
    }

    @Test
    fun `convert meters to feet`() {
        vm.setFromUnit(0)  // m
        vm.setToUnit(5)    // ft
        vm.updateInput(1.0)
        assertEquals(3.281, vm.result, 0.01)
    }

    // ── Weight conversions ──

    @Test
    fun `convert kilograms to pounds`() {
        vm.category = UnitCategory.WEIGHT
        vm.setFromUnit(0)  // kg
        vm.setToUnit(3)    // lb
        vm.updateInput(1.0)
        assertEquals(2.205, vm.result, 0.01)
    }

    @Test
    fun `convert grams to kilograms`() {
        vm.category = UnitCategory.WEIGHT
        vm.setFromUnit(1)  // g
        vm.setToUnit(0)    // kg
        vm.updateInput(500.0)
        assertEquals(0.5, vm.result, 0.001)
    }

    // ── Temperature conversions ──

    @Test
    fun `convert celsius to fahrenheit`() {
        vm.category = UnitCategory.TEMPERATURE
        vm.setFromUnit(0)  // C
        vm.setToUnit(1)    // F
        vm.updateInput(100.0)
        assertEquals(212.0, vm.result, 0.1)
    }

    @Test
    fun `convert fahrenheit to celsius`() {
        vm.category = UnitCategory.TEMPERATURE
        vm.setFromUnit(1)  // F
        vm.setToUnit(0)    // C
        vm.updateInput(32.0)
        assertEquals(0.0, vm.result, 0.1)
    }

    @Test
    fun `convert celsius to kelvin`() {
        vm.category = UnitCategory.TEMPERATURE
        vm.setFromUnit(0)  // C
        vm.setToUnit(2)    // K
        vm.updateInput(0.0)
        assertEquals(273.15, vm.result, 0.1)
    }

    // ── Speed conversions ──

    @Test
    fun `convert kmh to mph`() {
        vm.category = UnitCategory.SPEED
        vm.setFromUnit(1)  // km/h
        vm.setToUnit(2)    // mph
        vm.updateInput(100.0)
        assertEquals(62.137, vm.result, 0.01)
    }

    // ── Data conversions ──

    @Test
    fun `convert GB to MB`() {
        vm.category = UnitCategory.DATA
        vm.setFromUnit(3)  // GB
        vm.setToUnit(2)    // MB
        vm.updateInput(1.0)
        assertEquals(1024.0, vm.result, 0.001)
    }

    @Test
    fun `convert KB to bytes`() {
        vm.category = UnitCategory.DATA
        vm.setFromUnit(1)  // KB
        vm.setToUnit(0)    // B
        vm.updateInput(1.0)
        assertEquals(1024.0, vm.result, 0.001)
    }

    // ── Area conversions ──

    @Test
    fun `convert square meters to square kilometers`() {
        vm.category = UnitCategory.AREA
        vm.setFromUnit(0)  // m²
        vm.setToUnit(1)    // km²
        vm.updateInput(1000000.0)
        assertEquals(1.0, vm.result, 0.001)
    }

    // ── Swap units ──

    @Test
    fun `swapUnits exchanges from and to indices`() {
        vm.setFromUnit(2)
        vm.setToUnit(5)
        vm.swapUnits()
        assertEquals(5, vm.fromUnitIndex)
        assertEquals(2, vm.toUnitIndex)
    }

    // ── Input update ──

    @Test
    fun `updateInput sets value`() {
        vm.updateInput(42.0)
        assertEquals(42.0, vm.inputValue, 0.001)
    }

    @Test
    fun `updateInput handles negative`() {
        vm.updateInput(-15.0)
        assertEquals(-15.0, vm.inputValue, 0.001)
    }

    @Test
    fun `updateInput from string`() {
        vm.updateInput("42.5")
        assertEquals(42.5, vm.inputValue, 0.001)
    }

    @Test
    fun `updateInput from invalid string returns zero`() {
        vm.updateInput("abc")
        assertEquals(0.0, vm.inputValue, 0.001)
    }

    // ── Reset ──

    @Test
    fun `resetToDefaults clears state`() {
        vm.cycleCategory()
        vm.updateInput(99.0)
        vm.setFromUnit(3)
        vm.setToUnit(5)

        vm.resetToDefaults()

        assertEquals(UnitCategory.LENGTH, vm.category)
        assertEquals(0.0, vm.inputValue, 0.001)
        assertEquals(0, vm.fromUnitIndex)
        assertEquals(1, vm.toUnitIndex)
    }

    // ── Result formatting ──

    @Test
    fun `formatResult handles normal values`() {
        vm.setFromUnit(0)
        vm.setToUnit(5)
        vm.updateInput(1.0)
        val formatted = vm.formatResult()
        assertTrue(formatted.isNotEmpty())
        assertFalse(formatted.contains("E"))
    }

    @Test
    fun `formatResult handles zero`() {
        vm.updateInput(0.0)
        assertEquals("0", vm.formatResult())
    }
}
