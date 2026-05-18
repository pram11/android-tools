package com.armyknife.tools.data.registry

import com.armyknife.tools.core.common.Constants

/**
 * Central registry holding the dynamic list of all ToolItems.
 * New tools are registered here to auto-discover in Dashboard, Search, and Favorites.
 */
object ToolRegistry {

    private val _tools: List<ToolItem> = listOf(
        // Sensor Tools
        ToolItem(
            id = "compass",
            title = "Compass",
            description = "Geomagnetic field sensor compass with cardinal directions",
            category = ToolCategory.SENSOR,
            iconRes = "sensor",
            route = Constants.ROUTE_COMPASS
        ),
        ToolItem(
            id = "bubble_level",
            title = "Bubble Level",
            description = "Accelerometer-based digital spirit level",
            category = ToolCategory.SENSOR,
            iconRes = "sensor",
            route = Constants.ROUTE_BUBBLE_LEVEL
        ),

        // Hardware Tools
        ToolItem(
            id = "flashlight",
            title = "Flashlight & SOS",
            description = "Camera torch control with SOS signaling",
            category = ToolCategory.HARDWARE,
            iconRes = "hardware",
            route = Constants.ROUTE_FLASHLIGHT
        ),
        ToolItem(
            id = "qr_scanner",
            title = "QR/Barcode Scanner",
            description = "Scan and generate QR/barcode using CameraX",
            category = ToolCategory.HARDWARE,
            iconRes = "hardware",
            route = Constants.ROUTE_QR_SCANNER
        ),
        ToolItem(
            id = "magnifier",
            title = "Magnifier",
            description = "CameraX zoom control for magnification",
            category = ToolCategory.HARDWARE,
            iconRes = "hardware",
            route = Constants.ROUTE_MAGNIFIER
        ),
        ToolItem(
            id = "mirror",
            title = "Mirror",
            description = "Front camera rendering as a digital mirror",
            category = ToolCategory.HARDWARE,
            iconRes = "hardware",
            route = Constants.ROUTE_MIRROR
        ),

        // Media Tools
        ToolItem(
            id = "apk_extractor",
            title = "APK Extractor",
            description = "Extract APK info via PackageManager",
            category = ToolCategory.MEDIA,
            iconRes = "media",
            route = Constants.ROUTE_APK_EXTRACTOR
        ),
        ToolItem(
            id = "image_converter",
            title = "Image Converter",
            description = "Convert and compress images with Bitmap factory",
            category = ToolCategory.MEDIA,
            iconRes = "media",
            route = Constants.ROUTE_IMAGE_CONVERTER
        ),
        ToolItem(
            id = "pdf_utility",
            title = "PDF Utility",
            description = "PDF document rendering with PdfDocument",
            category = ToolCategory.MEDIA,
            iconRes = "media",
            route = Constants.ROUTE_PDF_UTILITY
        ),
        ToolItem(
            id = "voice_recorder",
            title = "Voice Recorder",
            description = "Local audio recording with MediaRecorder",
            category = ToolCategory.MEDIA,
            iconRes = "media",
            route = Constants.ROUTE_VOICE_RECORDER,
            requiredPermissions = listOf("android.permission.RECORD_AUDIO")
        ),

        // Utility Tools
        ToolItem(
            id = "unit_converter",
            title = "Unit Converter",
            description = "Convert between various units of measurement",
            category = ToolCategory.UTILITY,
            iconRes = "utility",
            route = Constants.ROUTE_UNIT_CONVERTER
        ),
        ToolItem(
            id = "calculators",
            title = "Calculators",
            description = "BMI, Age, Discount and financial calculators",
            category = ToolCategory.UTILITY,
            iconRes = "utility",
            route = Constants.ROUTE_CALCULATORS
        ),
        ToolItem(
            id = "text_crypto",
            title = "Text Crypto",
            description = "Base64, Hex, AES encryption/decryption",
            category = ToolCategory.UTILITY,
            iconRes = "utility",
            route = Constants.ROUTE_TEXT_CRYPTO
        ),
        ToolItem(
            id = "morse_code",
            title = "Morse Code",
            description = "Text to Morse with flash/vibration output",
            category = ToolCategory.UTILITY,
            iconRes = "utility",
            route = Constants.ROUTE_MORSE_CODE
        ),
        ToolItem(
            id = "random_generator",
            title = "Random Generator",
            description = "Generate random numbers, strings, and UUIDs",
            category = ToolCategory.UTILITY,
            iconRes = "utility",
            route = Constants.ROUTE_RANDOM_GENERATOR
        )
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
