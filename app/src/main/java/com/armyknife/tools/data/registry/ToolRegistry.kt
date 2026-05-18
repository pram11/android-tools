package com.armyknife.tools.data.registry

/**
 * Central registry holding the dynamic list of all ToolItems.
 * New tools are registered here to auto-discover in Dashboard, Search, and Favorites.
 */
object ToolRegistry {

    private val _tools: List<ToolItem> = listOf(
        // Tools will be appended as features are implemented
    )

    val tools: List<ToolItem> get() = _tools

    fun findByCategory(category: ToolCategory): List<ToolItem> =
        tools.filter { it.category == category }

    fun findById(id: String): ToolItem? =
        tools.find { it.id == id }

    fun search(query: String): List<ToolItem> {
        val lower = query.lowercase()
        return tools.filter {
            it.title.lowercase().contains(lower) ||
            it.description.lowercase().contains(lower)
        }
    }
}
