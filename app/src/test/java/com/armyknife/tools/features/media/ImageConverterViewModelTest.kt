package com.armyknife.tools.features.media

import org.junit.Assert.*
import org.junit.Test

class ImageConverterViewModelTest {

    private val vm = ImageConverterViewModel()

    // ── ImageFormat ──

    @Test
    fun `format extension maps correctly`() {
        assertEquals("png", ImageFormat.PNG.extension)
        assertEquals("jpeg", ImageFormat.JPEG.extension)
        assertEquals("webp", ImageFormat.WEBP.extension)
    }

    @Test
    fun `format mimeType maps correctly`() {
        assertEquals("image/png", ImageFormat.PNG.mimeType)
        assertEquals("image/jpeg", ImageFormat.JPEG.mimeType)
        assertEquals("image/webp", ImageFormat.WEBP.mimeType)
    }

    @Test
    fun `format supports lossless`() {
        assertTrue(ImageFormat.PNG.supportsLossless)
        assertFalse(ImageFormat.JPEG.supportsLossless)
        assertFalse(ImageFormat.WEBP.supportsLossless)
    }

    @Test
    fun `fromExtension returns correct format`() {
        assertEquals(ImageFormat.PNG, ImageFormat.fromExtension("png"))
        assertEquals(ImageFormat.JPEG, ImageFormat.fromExtension("jpg"))
        assertEquals(ImageFormat.JPEG, ImageFormat.fromExtension("jpeg"))
        assertEquals(ImageFormat.WEBP, ImageFormat.fromExtension("webp"))
    }

    @Test
    fun `fromExtension returns PNG for unknown`() {
        assertEquals(ImageFormat.PNG, ImageFormat.fromExtension("bmp"))
        assertEquals(ImageFormat.PNG, ImageFormat.fromExtension("gif"))
    }

    // ── Quality clamping ──

    @Test
    fun `quality clamps to 0-100 range`() {
        assertEquals(50, vm.defaultQuality)
        assertEquals(0, vm.clampQuality(-10))
        assertEquals(100, vm.clampQuality(150))
        assertEquals(75, vm.clampQuality(75))
    }

    // ── Output filename generation ──

    @Test
    fun `output filename uses new extension`() {
        val name = vm.generateOutputFilename("photo", ImageFormat.JPEG)
        assertEquals("photo.jpeg", name)
    }

    @Test
    fun `output filename preserves base with dots`() {
        val name = vm.generateOutputFilename("my.photo", ImageFormat.PNG)
        assertEquals("my.photo.png", name)
    }

    // ── Aspect ratio calculation ──

    @Test
    fun `calculateDimensions maintains aspect ratio when only width set`() {
        val (w, h) = vm.calculateDimensions(200, 300, 100, 0, true)
        assertEquals(100, w)
        assertEquals(150, h)
    }

    @Test
    fun `calculateDimensions maintains aspect ratio when only height set`() {
        val (w, h) = vm.calculateDimensions(200, 300, 0, 150, true)
        assertEquals(100, w)
        assertEquals(150, h)
    }

    @Test
    fun `calculateDimensions returns original when both 0`() {
        val (w, h) = vm.calculateDimensions(200, 300, 0, 0, true)
        assertEquals(200, w)
        assertEquals(300, h)
    }

    @Test
    fun `calculateDimensions ignores aspect ratio when disabled`() {
        val (w, h) = vm.calculateDimensions(200, 300, 100, 500, false)
        assertEquals(100, w)
        assertEquals(500, h)
    }

    @Test
    fun `calculateDimensions handles zero original dimensions`() {
        val (w, h) = vm.calculateDimensions(0, 0, 100, 200, false)
        assertEquals(100, w)
        assertEquals(200, h)
    }

    // ── Estimated file size ──

    @Test
    fun `estimateFileSize returns positive for JPEG`() {
        val size = vm.estimateFileSize(1920, 1080, ImageFormat.JPEG, 80)
        assertTrue(size > 0)
    }

    @Test
    fun `estimateFileSize smaller quality yields smaller size`() {
        val big = vm.estimateFileSize(1920, 1080, ImageFormat.JPEG, 95)
        val small = vm.estimateFileSize(1920, 1080, ImageFormat.JPEG, 10)
        assertTrue(small < big)
    }

    @Test
    fun `estimateFileSize larger dimensions yield larger size`() {
        val small = vm.estimateFileSize(640, 480, ImageFormat.JPEG, 80)
        val big = vm.estimateFileSize(3840, 2160, ImageFormat.JPEG, 80)
        assertTrue(big > small)
    }

    @Test
    fun `estimateFileSize PNG is lossless baseline`() {
        val png = vm.estimateFileSize(1920, 1080, ImageFormat.PNG, 100)
        assertTrue(png > 0)
    }

    // ── Compression ratio ──

    @Test
    fun `compressionRatio returns percentage`() {
        val ratio = vm.computeCompressionRatio(100000, 50000)
        assertEquals(50.0f, ratio, 0.01f)
    }

    @Test
    fun `compressionRatio zero original returns 0`() {
        val ratio = vm.computeCompressionRatio(0, 50000)
        assertEquals(0.0f, ratio, 0.01f)
    }

    @Test
    fun `compressionRatio larger output returns negative`() {
        val ratio = vm.computeCompressionRatio(50000, 100000)
        assertEquals(-100.0f, ratio, 0.01f)
    }

    // ── FormatCycle ──

    @Test
    fun `cycleFormat iterates through formats`() {
        vm.targetFormat = ImageFormat.PNG
        vm.cycleFormat()
        assertEquals(ImageFormat.JPEG, vm.targetFormat)
        vm.cycleFormat()
        assertEquals(ImageFormat.WEBP, vm.targetFormat)
        vm.cycleFormat()
        assertEquals(ImageFormat.PNG, vm.targetFormat)
    }

    // ── State mutations ──

    @Test
    fun `updateQuality clamps and sets state`() {
        vm.updateQuality(150)
        assertEquals(100, vm.quality)
        vm.updateQuality(-20)
        assertEquals(0, vm.quality)
    }

    @Test
    fun `updateDimensions sets values`() {
        vm.updateResizeWidth(800)
        assertEquals(800, vm.resizeWidth)
        vm.updateResizeHeight(600)
        assertEquals(600, vm.resizeHeight)
    }

    @Test
    fun `toggleMaintainAspect flips boolean`() {
        vm.toggleMaintainAspect()
        assertFalse(vm.maintainAspect)
        vm.toggleMaintainAspect()
        assertTrue(vm.maintainAspect)
    }

    @Test
    fun `resetToDefaults clears all values`() {
        vm.targetFormat = ImageFormat.WEBP
        vm.quality = 10
        vm.resizeWidth = 500
        vm.resizeHeight = 500
        vm.maintainAspect = false
        vm.resetToDefaults()
        assertEquals(ImageFormat.PNG, vm.targetFormat)
        assertEquals(50, vm.quality)
        assertEquals(0, vm.resizeWidth)
        assertEquals(0, vm.resizeHeight)
        assertTrue(vm.maintainAspect)
    }

    // ── ConversionConfig ──

    @Test
    fun `buildConfig produces correct data class`() {
        vm.targetFormat = ImageFormat.JPEG
        vm.quality = 70
        vm.resizeWidth = 800
        vm.resizeHeight = 600
        vm.maintainAspect = true

        val config = vm.buildConfig(2400, 1800)
        assertEquals(ImageFormat.JPEG, config.format)
        assertEquals(70, config.quality)
        assertEquals(800, config.width)
        assertEquals(600, config.height)
        assertTrue(config.maintainAspect)
        assertEquals(2400, config.originalWidth)
        assertEquals(1800, config.originalHeight)
    }
}
