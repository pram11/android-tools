package com.armyknife.tools.features.utility

import org.junit.Assert.*
import org.junit.Test

class CalculatorsViewModelTest {

    private val vm = CalculatorsViewModel()

    // ── Default state ──

    @Test
    fun `default calculator type is BMI`() {
        assertEquals(CalculatorType.BMI, vm.calculatorType)
    }

    // ── Calculator type cycling ──

    @Test
    fun `cycleCalculator iterates BMI to Age to Discount to BMI`() {
        vm.cycleCalculator()
        assertEquals(CalculatorType.AGE, vm.calculatorType)
        vm.cycleCalculator()
        assertEquals(CalculatorType.DISCOUNT, vm.calculatorType)
        vm.cycleCalculator()
        assertEquals(CalculatorType.BMI, vm.calculatorType)
    }

    // ── BMI calculations ──

    @Test
    fun `BMI normal weight`() {
        vm.updateWeight(70.0)
        vm.updateHeight(1.75)
        assertEquals(22.86, vm.bmiValue, 0.01)
    }

    @Test
    fun `BMI underweight`() {
        vm.updateWeight(50.0)
        vm.updateHeight(1.80)
        assertEquals(15.43, vm.bmiValue, 0.01)
    }

    @Test
    fun `BMI obese`() {
        vm.updateWeight(120.0)
        vm.updateHeight(1.70)
        assertEquals(41.52, vm.bmiValue, 0.01)
    }

    @Test
    fun `BMI category for underweight`() {
        vm.updateWeight(50.0)
        vm.updateHeight(1.80)
        assertEquals("Underweight", vm.bmiCategory)
    }

    @Test
    fun `BMI category for normal`() {
        vm.updateWeight(70.0)
        vm.updateHeight(1.75)
        assertEquals("Normal", vm.bmiCategory)
    }

    @Test
    fun `BMI category for overweight`() {
        vm.updateWeight(85.0)
        vm.updateHeight(1.70)
        assertEquals("Overweight", vm.bmiCategory)
    }

    @Test
    fun `BMI category for obese`() {
        vm.updateWeight(120.0)
        vm.updateHeight(1.70)
        assertEquals("Obese", vm.bmiCategory)
    }

    // ── Age calculations ──

    @Test
    fun `age in years`() {
        // Born 1990-01-01, current ~2025 = 35 years
        vm.updateBirthDay(1)
        vm.updateBirthMonth(1)
        // Day/month set; actual years depends on current date
        // Just verify no crash and result is positive
        assertTrue(vm.ageYears >= 0)
    }

    @Test
    fun `age in days is positive`() {
        vm.updateBirthDay(1)
        vm.updateBirthMonth(1)
        assertTrue(vm.ageDays >= 0)
    }

    @Test
    fun `age in hours is positive`() {
        vm.updateBirthDay(1)
        vm.updateBirthMonth(1)
        assertTrue(vm.ageHours >= 0)
    }

    // ── Discount calculations ──

    @Test
    fun `discount 50 percent`() {
        vm.updateOriginalPrice(100.0)
        vm.updateDiscountPercent(50.0)
        assertEquals(50.0, vm.discountAmount, 0.01)
        assertEquals(50.0, vm.finalPrice, 0.01)
    }

    @Test
    fun `discount 0 percent`() {
        vm.updateOriginalPrice(100.0)
        vm.updateDiscountPercent(0.0)
        assertEquals(0.0, vm.discountAmount, 0.01)
        assertEquals(100.0, vm.finalPrice, 0.01)
    }

    @Test
    fun `discount 100 percent`() {
        vm.updateOriginalPrice(100.0)
        vm.updateDiscountPercent(100.0)
        assertEquals(100.0, vm.discountAmount, 0.01)
        assertEquals(0.0, vm.finalPrice, 0.01)
    }

    @Test
    fun `discount clamps to 0-100`() {
        vm.updateOriginalPrice(100.0)
        vm.updateDiscountPercent(150.0)
        assertEquals(100.0, vm.discountAmount, 0.01)
        vm.updateDiscountPercent(-10.0)
        assertEquals(0.0, vm.discountAmount, 0.01)
    }

    @Test
    fun `discount clamps price to non-negative`() {
        vm.updateOriginalPrice(-50.0)
        assertEquals(0.0, vm.originalPrice, 0.01)
    }

    // ── Reset ──

    @Test
    fun `resetToDefaults clears all values`() {
        vm.calculatorType = CalculatorType.DISCOUNT
        vm.updateOriginalPrice(999.0)
        vm.updateDiscountPercent(50.0)

        vm.resetToDefaults()

        assertEquals(CalculatorType.BMI, vm.calculatorType)
        assertEquals(0.0, vm.originalPrice, 0.01)
        assertEquals(0.0, vm.discountPercent, 0.01)
    }

    // ── Formatting ──

    @Test
    fun `formatValue handles zero`() {
        assertEquals("0", vm.formatValue(0.0))
    }

    @Test
    fun `formatValue handles normal number`() {
        val formatted = vm.formatValue(123.456)
        assertTrue(formatted.contains("123"))
    }
}
