package com.armyknife.tools.data.registry

/**
 * Immutable category definitions for the tool registry.
 */
enum class ToolCategory(val id: String, val title: String) {
    SENSOR("sensor", "Sensor Tools"),
    HARDWARE("hardware", "Hardware Control"),
    MEDIA("media", "Media & Files"),
    UTILITY("utility", "Data & Utilities")
}
