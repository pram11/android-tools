package com.armyknife.tools.features.utility

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

/**
 * Calculator types available in the Financial and Lifestyle Calculators tool.
 */
enum class CalculatorType(val label: String) {
    BMI("BMI Calculator"),
    AGE("Age Calculator"),
    DISCOUNT("Discount Calculator")
}

/**
 * ViewModel for the Calculators tool.
 * Manages BMI, Age, and Discount calculations.
 */
class CalculatorsViewModel : ViewModel() {

    var calculatorType: CalculatorType = CalculatorType.BMI
        internal set

    // ── BMI fields ──
    var weightKg: Double by mutableDoubleStateOf(0.0)
        private set
    var heightM: Double by mutableDoubleStateOf(0.0)
        private set

    // ── Age fields ──
    var birthDay: Int by mutableIntStateOf(1)
        private set
    var birthMonth: Int by mutableIntStateOf(1)
        private set

    // ── Discount fields ──
    var originalPrice: Double by mutableDoubleStateOf(0.0)
        private set
    var discountPercent: Double by mutableDoubleStateOf(0.0)
        private set

    // ── Computed values ──

    val bmiValue: Double
        get() = if (heightM > 0) weightKg / (heightM * heightM) else 0.0

    val bmiCategory: String
        get() {
            val bmi = bmiValue
            return when {
                bmi < 18.5 -> "Underweight"
                bmi < 25.0 -> "Normal"
                bmi < 30.0 -> "Overweight"
                else -> "Obese"
            }
        }

    val ageYears: Int
        get() {
            val birth = LocalDate.of(1990, birthMonth.coerceIn(1, 12), birthDay.coerceIn(1, 28))
            val now = LocalDate.now()
            return Period.between(birth, now).years
        }

    val ageDays: Long
        get() {
            val birth = LocalDate.of(1990, birthMonth.coerceIn(1, 12), birthDay.coerceIn(1, 28))
            val now = LocalDate.now()
            return ChronoUnit.DAYS.between(birth, now)
        }

    val ageHours: Long
        get() = ageDays * 24

    val discountAmount: Double
        get() {
            val pct = discountPercent.coerceIn(0.0, 100.0)
            return originalPrice * pct / 100.0
        }

    val finalPrice: Double
        get() = originalPrice - discountAmount

    // ── Calculator selection ──

    fun cycleCalculator() {
        calculatorType = when (calculatorType) {
            CalculatorType.BMI -> CalculatorType.AGE
            CalculatorType.AGE -> CalculatorType.DISCOUNT
            CalculatorType.DISCOUNT -> CalculatorType.BMI
        }
    }

    // ── BMI setters ──

    fun updateWeight(value: Double) {
        weightKg = value.coerceAtLeast(0.0)
    }

    fun updateHeight(value: Double) {
        heightM = value.coerceAtLeast(0.0)
    }

    // ── Age setters ──

    fun updateBirthDay(value: Int) {
        birthDay = value.coerceIn(1, 28)
    }

    fun updateBirthMonth(value: Int) {
        birthMonth = value.coerceIn(1, 12)
    }

    // ── Discount setters ──

    fun updateOriginalPrice(value: Double) {
        originalPrice = value.coerceAtLeast(0.0)
    }

    fun updateDiscountPercent(value: Double) {
        discountPercent = value.coerceIn(0.0, 100.0)
    }

    // ── Formatting ──

    fun formatValue(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            "%.2f".format(value).trimEnd('0').trimEnd('.')
        }
    }

    // ── Reset ──

    fun resetToDefaults() {
        calculatorType = CalculatorType.BMI
        weightKg = 0.0
        heightM = 0.0
        birthDay = 1
        birthMonth = 1
        originalPrice = 0.0
        discountPercent = 0.0
    }
}
