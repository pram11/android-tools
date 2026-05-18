package com.armyknife.tools.features.media

import org.junit.Assert.*
import org.junit.Test

class ApkExtractorViewModelTest {

    private val vm = ApkExtractorViewModel()

    @Test
    fun `initial search query is empty`() {
        assertTrue(vm.searchQuery.isEmpty())
    }

    @Test
    fun `update search query sets value`() {
        vm.onSearchQueryChanged("whatsapp")
        assertEquals("whatsapp", vm.searchQuery)
    }

    @Test
    fun `clear search query empties value`() {
        vm.onSearchQueryChanged("test")
        vm.onSearchQueryChanged("")
        assertTrue(vm.searchQuery.isEmpty())
    }

    @Test
    fun `sort order defaults to name`() {
        assertEquals(ApkSortOrder.NAME, vm.sortOrder)
    }

    @Test
    fun `cycling sort order goes name size date`() {
        assertEquals(ApkSortOrder.NAME, vm.sortOrder)
        vm.cycleSortOrder()
        assertEquals(ApkSortOrder.SIZE, vm.sortOrder)
        vm.cycleSortOrder()
        assertEquals(ApkSortOrder.DATE, vm.sortOrder)
        vm.cycleSortOrder()
        assertEquals(ApkSortOrder.NAME, vm.sortOrder)
    }

    @Test
    fun `format bytes shows bytes`() {
        assertEquals("0 B", ApkExtractorViewModel.formatBytes(0L))
    }

    @Test
    fun `format bytes shows KB`() {
        assertEquals("1.5 KB", ApkExtractorViewModel.formatBytes(1536L))
    }

    @Test
    fun `format bytes shows MB`() {
        assertEquals("10.2 MB", ApkExtractorViewModel.formatBytes(10706580L))
    }

    @Test
    fun `format bytes shows GB`() {
        assertEquals("1.5 GB", ApkExtractorViewModel.formatBytes(1610612736L))
    }

    @Test
    fun `apk info implements equals`() {
        val a = ApkInfo("com.test", "Test App", "1.0", 1024, 1000L)
        val b = ApkInfo("com.test", "Test App", "1.0", 1024, 1000L)
        assertEquals(a, b)
    }

    @Test
    fun `apk info implements compareTo by name`() {
        val a = ApkInfo("com.a", "Alpha", "1.0", 1024, 1000L)
        val b = ApkInfo("com.b", "Beta", "1.0", 1024, 1000L)
        assertTrue(a < b)
    }

    @Test
    fun `filter apps returns matching results`() {
        val apps = listOf(
            ApkInfo("com.whatsapp", "WhatsApp", "2.0", 50000, 1000L),
            ApkInfo("com.facebook", "Facebook", "3.0", 80000, 2000L),
        )
        val filtered = vm.filterApps(apps, "what")
        assertEquals(1, filtered.size)
        assertEquals("com.whatsapp", filtered[0].packageName)
    }

    @Test
    fun `filter apps with empty query returns all`() {
        val apps = listOf(
            ApkInfo("com.a", "A", "1.0", 100, 1000L),
            ApkInfo("com.b", "B", "1.0", 200, 2000L),
        )
        val filtered = vm.filterApps(apps, "")
        assertEquals(2, filtered.size)
    }

    @Test
    fun `filter apps is case insensitive`() {
        val apps = listOf(
            ApkInfo("com.whatsapp", "WhatsApp", "2.0", 50000, 1000L),
        )
        val filtered = vm.filterApps(apps, "WHAT")
        assertEquals(1, filtered.size)
    }

    @Test
    fun `sort by name orders alphabetically`() {
        val apps = listOf(
            ApkInfo("com.z", "Zapp", "1.0", 100, 1000L),
            ApkInfo("com.a", "Aapp", "1.0", 200, 2000L),
            ApkInfo("com.m", "Mapp", "1.0", 150, 1500L),
        )
        val sorted = vm.sortApps(apps, ApkSortOrder.NAME)
        assertEquals("Aapp", sorted[0].appName)
        assertEquals("Mapp", sorted[1].appName)
        assertEquals("Zapp", sorted[2].appName)
    }

    @Test
    fun `sort by size orders ascending`() {
        val apps = listOf(
            ApkInfo("com.a", "A", "1.0", 300, 1000L),
            ApkInfo("com.b", "B", "1.0", 100, 2000L),
            ApkInfo("com.c", "C", "1.0", 200, 1500L),
        )
        val sorted = vm.sortApps(apps, ApkSortOrder.SIZE)
        assertEquals(100, sorted[0].sizeBytes)
        assertEquals(200, sorted[1].sizeBytes)
        assertEquals(300, sorted[2].sizeBytes)
    }

    @Test
    fun `sort by date orders ascending`() {
        val apps = listOf(
            ApkInfo("com.a", "A", "1.0", 100, 3000L),
            ApkInfo("com.b", "B", "1.0", 100, 1000L),
            ApkInfo("com.c", "C", "1.0", 100, 2000L),
        )
        val sorted = vm.sortApps(apps, ApkSortOrder.DATE)
        assertEquals(1000, sorted[0].installTime)
        assertEquals(2000, sorted[1].installTime)
        assertEquals(3000, sorted[2].installTime)
    }
}
