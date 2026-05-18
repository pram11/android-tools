package com.armyknife.tools.features.utility

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Conversion categories.
 */
enum class UnitCategory(val label: String) {
    LENGTH("Length"),
    WEIGHT("Weight"),
    TEMPERATURE("Temperature"),
    SPEED("Speed"),
    DATA("Data"),
    AREA("Area")
}

/**
 * Unit definition with name and conversion factor to base unit.
 * For temperature, special handling is needed (not simple multiply).
 */
data class UnitDef(
    val name: String,
    /** Conversion factor: baseUnit = value × factor */
    val factor: Double = 1.0,
    /** Offset for temperature conversions (applied before factor) */
    val offset: Double = 0.0,
    val isTemperature: Boolean = false
)

/**
 * All unit definitions grouped by category.
 */
object UnitDefinitions {
    val LENGTH = listOf(
        UnitDef("m", 1.0),           // meter (base)
        UnitDef("km", 1000.0),       // kilometer
        UnitDef("cm", 0.01),         // centimeter
        UnitDef("mm", 0.001),        // millimeter
        UnitDef("mi", 1609.344),     // mile
        UnitDef("ft", 0.3048),       // foot
        UnitDef("in", 0.0254),       // inch
        UnitDef("yd", 0.9144),       // yard
    )

    val WEIGHT = listOf(
        UnitDef("kg", 1.0),          // kilogram (base)
        UnitDef("g", 0.001),         // gram
        UnitDef("mg", 0.000001),     // milligram
        UnitDef("lb", 0.453592),     // pound
        UnitDef("oz", 0.0283495),    // ounce
    )

    val TEMPERATURE = listOf(
        UnitDef("°C", 1.0, 0.0, true),   // Celsius (base)
        UnitDef("°F", 1.0, 32.0, true),  // Fahrenheit
        UnitDef("K", 1.0, 273.15, true), // Kelvin
    )

    val SPEED = listOf(
        UnitDef("m/s", 1.0),           // meters per second (base)
        UnitDef("km/h", 1.0 / 3.6),    // kilometers per hour
        UnitDef("mph", 0.44704),       // miles per hour
        UnitDef("knots", 0.514444),    // knots
    )

    val DATA = listOf(
        UnitDef("B", 1.0),             // byte (base)
        UnitDef("KB", 1024.0),         // kilobyte
        UnitDef("MB", 1024.0 * 1024),  // megabyte
        UnitDef("GB", 1024.0 * 1024 * 1024), // gigabyte
        UnitDef("TB", 1024.0 * 1024 * 1024 * 1024), // terabyte
    )

    val AREA = listOf(
        UnitDef("m²", 1.0),             // square meter (base)
        UnitDef("km²", 1000000.0),      // square kilometer
        UnitDef("ha", 10000.0),         // hectare
        UnitDef("acre", 4046.856422),   // acre
        UnitDef("ft²", 0.092903),       // square foot
    )

    fun getUnits(category: UnitCategory): List<UnitDef> = when (category) {
        UnitCategory.LENGTH -> LENGTH
        UnitCategory.WEIGHT -> WEIGHT
        UnitCategory.TEMPERATURE -> TEMPERATURE
        UnitCategory.SPEED -> SPEED
        UnitCategory.DATA -> DATA
        UnitCategory.AREA -> AREA
    }
}

/**
 * ViewModel for the Unit Converter tool.
 * Manages category selection, unit selection, and conversion computation.
 */
class UnitConverterViewModel : ViewModel() {

    var category: UnitCategory = UnitCategory.LENGTH
        set(value) {
            field = value
            fromUnitIndex = 0
            toUnitIndex = 1
        }

    var inputValue: Double = 0.0
        private set

    var fromUnitIndex: Int = 0
        private set

    var toUnitIndex: Int = 1
        private set

    val result: Double
        get() = computeResult()

    // ── Category ──

    fun cycleCategory() {
        category = when (category) {
            UnitCategory.LENGTH -> UnitCategory.WEIGHT
            UnitCategory.WEIGHT -> UnitCategory.TEMPERATURE
            UnitCategory.TEMPERATURE -> UnitCategory.SPEED
            UnitCategory.SPEED -> UnitCategory.DATA
            UnitCategory.DATA -> UnitCategory.AREA
            UnitCategory.AREA -> UnitCategory.LENGTH
        }
        fromUnitIndex = 0
        toUnitIndex = 1
    }

    // ── Input ──

    fun updateInput(value: Double) {
        inputValue = value
    }

    fun updateInput(text: String) {
        inputValue = text.toDoubleOrNull() ?: 0.0
    }

    // ── Unit selection ──

    fun setFromUnit(index: Int) {
        val units = UnitDefinitions.getUnits(category)
        fromUnitIndex = index.coerceIn(0, units.size - 1)
    }

    fun setToUnit(index: Int) {
        val units = UnitDefinitions.getUnits(category)
        toUnitIndex = index.coerceIn(0, units.size - 1)
    }

    fun swapUnits() {
        val temp = fromUnitIndex
        fromUnitIndex = toUnitIndex
        toUnitIndex = temp
    }

    // ── Conversion logic ──

    private fun computeResult(): Double {
        val units = UnitDefinitions.getUnits(category)
        val from = units[fromUnitIndex]
        val to = units[toUnitIndex]

        if (from.isTemperature) {
            return convertTemperature(inputValue, from, to)
        }

        // Standard conversion: value × fromFactor / toFactor
        val baseValue = inputValue * from.factor
        return baseValue / to.factor
    }

    private fun convertTemperature(value: Double, from: UnitDef, to: UnitDef): Double {
        // Step 1: convert to Celsius (base for temperature)
        val celsius = when {
            from.name == "°C" -> value
            from.name == "°F" -> (value - 32.0) * 5.0 / 9.0
            from.name == "K" -> value - 273.15
            else -> value
        }
        // Step 2: convert from Celsius to target
        return when {
            to.name == "°C" -> celsius
            to.name == "°F" -> celsius * 9.0 / 5.0 + 32.0
            to.name == "K" -> celsius + 273.15
            else -> celsius
        }
    }

    // ── Formatting ──

    fun formatResult(): String {
        val r = result
        return when {
            r == 0.0 -> "0"
            r > 0.0001 && r < 1000000 -> "%.6f".format(r).trimEnd('0').trimEnd('.')
            else -> "%.6e".format(r)
        }
    }

    // ── Reset ──

    fun resetToDefaults() {
        category = UnitCategory.LENGTH
        inputValue = 0.0
        fromUnitIndex = 0
        toUnitIndex = 1
    }
}
