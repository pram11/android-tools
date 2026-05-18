package com.armyknife.tools.features.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Supported image output formats.
 */
enum class ImageFormat(
    val extension: String,
    val mimeType: String,
    val supportsLossless: Boolean
) {
    PNG("png", "image/png", true),
    JPEG("jpeg", "image/jpeg", false),
    WEBP("webp", "image/webp", false);

    companion object {
        /**
         * Map file extension string to ImageFormat. Defaults to PNG.
         */
        fun fromExtension(ext: String): ImageFormat {
            return when (ext.lowercase()) {
                "png" -> PNG
                "jpg", "jpeg" -> JPEG
                "webp" -> WEBP
                else -> PNG
            }
        }
    }
}

/**
 * Conversion configuration produced from user settings + original image dimensions.
 */
data class ConversionConfig(
    val format: ImageFormat,
    val quality: Int,
    val width: Int,
    val height: Int,
    val maintainAspect: Boolean,
    val originalWidth: Int,
    val originalHeight: Int
)

/**
 * ViewModel for the Image Converter & Compressor tool.
 * Manages format selection, quality, resize, and aspect ratio state.
 */
class ImageConverterViewModel : ViewModel() {

    var targetFormat: ImageFormat = ImageFormat.PNG
        internal set

    var quality by mutableIntStateOf(50)
        internal set

    var resizeWidth by mutableIntStateOf(0)
        internal set

    var resizeHeight by mutableIntStateOf(0)
        internal set

    var maintainAspect by mutableStateOf(true)
        internal set

    val defaultQuality: Int get() = 50



    // ── Quality ──

    /**
     * Clamp quality value to 0-100 range.
     */
    fun clampQuality(value: Int): Int = value.coerceIn(0, 100)

    /**
     * Update quality, clamping to valid range.
     */
    fun updateQuality(value: Int) {
        quality = clampQuality(value)
    }

    // ── Filename ──

    /**
     * Generate output filename from base name and target format.
     */
    fun generateOutputFilename(baseName: String, format: ImageFormat): String =
        "$baseName.${format.extension}"

    // ── Dimension calculation ──

    /**
     * Calculate output dimensions.
     * When maintainAspect is true and only one dimension is provided,
     * the other is computed from the original aspect ratio.
     */
    fun calculateDimensions(
        originalWidth: Int,
        originalHeight: Int,
        targetWidth: Int,
        targetHeight: Int,
        maintainAspect: Boolean
    ): Pair<Int, Int> {
        if (!maintainAspect) {
            return if (targetWidth > 0 && targetHeight > 0) {
                targetWidth to targetHeight
            } else if (targetWidth > 0) {
                targetWidth to originalHeight
            } else if (targetHeight > 0) {
                originalWidth to targetHeight
            } else {
                originalWidth to originalHeight
            }
        }

        // maintainAspect mode
        if (targetWidth > 0 && targetHeight > 0) {
            return targetWidth to targetHeight
        }
        if (targetWidth > 0 && originalWidth > 0) {
            return targetWidth to (targetWidth * originalHeight / originalWidth)
        }
        if (targetHeight > 0 && originalHeight > 0) {
            return (targetHeight * originalWidth / originalHeight) to targetHeight
        }
        return originalWidth to originalHeight
    }

    // ── File size estimation ──

    /**
     * Estimate output file size in bytes based on dimensions, format, and quality.
     * Pure estimation — actual size depends on image content.
     */
    fun estimateFileSize(width: Int, height: Int, format: ImageFormat, quality: Int): Long {
        val pixelCount = (width * height).toLong()
        val qualityFactor = quality.coerceIn(1, 100) / 100.0

        return when (format) {
            ImageFormat.PNG -> pixelCount * 3L // ~3 bytes/pixel lossless
            ImageFormat.JPEG -> (pixelCount * 3 * qualityFactor / 4).toLong().coerceAtLeast(100)
            ImageFormat.WEBP -> (pixelCount * 2 * qualityFactor / 3).toLong().coerceAtLeast(100)
        }
    }

    // ── Compression ratio ──

    /**
     * Compute compression ratio as percentage reduction.
     * Positive = file shrunk, Negative = file grew.
     */
    fun computeCompressionRatio(originalSize: Long, newSize: Long): Float {
        if (originalSize <= 0) return 0f
        return ((originalSize - newSize).toFloat() / originalSize.toFloat()) * 100f
    }

    // ── Format cycling ──

    /**
     * Cycle through formats: PNG → JPEG → WEBP → PNG.
     */
    fun cycleFormat() {
        targetFormat = when (targetFormat) {
            ImageFormat.PNG -> ImageFormat.JPEG
            ImageFormat.JPEG -> ImageFormat.WEBP
            ImageFormat.WEBP -> ImageFormat.PNG
        }
    }

    // ── Dimension state ──

    fun updateResizeWidth(value: Int) {
        resizeWidth = value
    }

    fun updateResizeHeight(value: Int) {
        resizeHeight = value
    }

    fun toggleMaintainAspect() {
        maintainAspect = !maintainAspect
    }

    // ── Reset ──

    fun resetToDefaults() {
        targetFormat = ImageFormat.PNG
        quality = defaultQuality
        resizeWidth = 0
        resizeHeight = 0
        maintainAspect = true
    }

    // ── Config builder ──

    /**
     * Build a ConversionConfig from current state and original dimensions.
     */
    fun buildConfig(originalWidth: Int, originalHeight: Int): ConversionConfig {
        val (w, h) = if (resizeWidth > 0 || resizeHeight > 0) {
            calculateDimensions(originalWidth, originalHeight, resizeWidth, resizeHeight, maintainAspect)
        } else {
            originalWidth to originalHeight
        }

        return ConversionConfig(
            format = targetFormat,
            quality = quality,
            width = w,
            height = h,
            maintainAspect = maintainAspect,
            originalWidth = originalWidth,
            originalHeight = originalHeight
        )
    }
}
