package com.armyknife.tools.data.registry

/**
 * Immutable specification model for a single tool entry.
 */
data class ToolItem(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val iconRes: String,
    val route: String,
    val requiredPermissions: List<String> = emptyList()
)
