package com.armyknife.tools.features.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Sort order for the APK list.
 */
enum class ApkSortOrder {
    NAME, SIZE, DATE
}

/**
 * Data class representing an installed APK.
 */
data class ApkInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val sizeBytes: Long,
    val installTime: Long
) : Comparable<ApkInfo> {
    override fun compareTo(other: ApkInfo): Int =
        appName.compareTo(other.appName, ignoreCase = true)
}

/**
 * ViewModel for the APK Extractor tool.
 * Manages search query, sort order, and APK list filtering.
 */
class ApkExtractorViewModel : ViewModel() {

    companion object {
        /**
         * Format byte size to human-readable string.
         */
        fun formatBytes(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
                bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
                else -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
            }
        }
    }

    var searchQuery by mutableStateOf("")
        private set

    var sortOrder: ApkSortOrder = ApkSortOrder.NAME
        private set

    /**
     * Update search query from text field.
     */
    fun onSearchQueryChanged(query: String) {
        searchQuery = query
    }

    /**
     * Cycle through sort orders: NAME → SIZE → DATE → NAME.
     */
    fun cycleSortOrder() {
        sortOrder = when (sortOrder) {
            ApkSortOrder.NAME -> ApkSortOrder.SIZE
            ApkSortOrder.SIZE -> ApkSortOrder.DATE
            ApkSortOrder.DATE -> ApkSortOrder.NAME
        }
    }

    /**
     * Filter apps by search query (case-insensitive, matches name or package).
     */
    fun filterApps(apps: List<ApkInfo>, query: String): List<ApkInfo> {
        if (query.isBlank()) return apps
        val lower = query.lowercase()
        return apps.filter {
            it.appName.lowercase().contains(lower) ||
            it.packageName.lowercase().contains(lower)
        }
    }

    /**
     * Sort apps by the current sort order.
     */
    fun sortApps(apps: List<ApkInfo>, order: ApkSortOrder): List<ApkInfo> {
        return when (order) {
            ApkSortOrder.NAME -> apps.sortedBy { it.appName.lowercase() }
            ApkSortOrder.SIZE -> apps.sortedBy { it.sizeBytes }
            ApkSortOrder.DATE -> apps.sortedBy { it.installTime }
        }
    }
}
