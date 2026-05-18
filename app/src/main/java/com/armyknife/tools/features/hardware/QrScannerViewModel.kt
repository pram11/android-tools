package com.armyknife.tools.features.hardware

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Encode text into a QR code matrix. Pure function, testable on JVM.
 * Returns null if input is blank.
 */
fun encodeQrMatrix(input: String, size: Int = 512): BooleanArray? {
    val text = input.trim()
    if (text.isBlank()) return null

    return try {
        val hints = mapOf<EncodeHintType, Any>(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1
        )
        val writer = QRCodeWriter()
        val matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints)

        val boolArray = BooleanArray(size * size)
        for (x in 0 until size) {
            for (y in 0 until size) {
                boolArray[x * size + y] = matrix[x, y]
            }
        }
        boolArray
    } catch (e: Exception) {
        null
    }
}

/**
 * Convert a QR code boolean matrix to an Android Bitmap.
 */
fun qrMatrixToBitmap(matrix: BooleanArray, size: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (matrix[x * size + y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}

/**
 * Scanner mode: scan barcodes or generate QR codes.
 */
enum class QrScannerMode {
    SCAN, GENERATE
}

/**
 * UI state for the QR Scanner screen.
 */
data class QrScannerUiState(
    val mode: QrScannerMode = QrScannerMode.SCAN,
    val scanResults: List<String> = emptyList(),
    val generatedQrBitmap: Bitmap? = null
)

/**
 * ViewModel for the QR Scanner tool.
 * Handles barcode scan results, QR code generation, and mode switching.
 */
class QrScannerViewModel : ViewModel() {

    private val _scanResults = mutableStateListOf<String>()
    var uiState by mutableStateOf(QrScannerUiState())
        private set

    var qrInput by mutableStateOf("")
        private set

    /**
     * Map barcode format string to a human-readable label.
     */
    companion object {
        fun formatLabel(format: String): String = when (format) {
            "QR_CODE" -> "QR Code"
            "DATA_MATRIX" -> "Data Matrix"
            "PDF_417" -> "PDF 417"
            "AZTEC" -> "Aztec"
            "UPC_A" -> "UPC-A"
            "UPC_E" -> "UPC-E"
            "EAN_13" -> "EAN-13"
            "EAN_8" -> "EAN-8"
            "CODE_128" -> "Code 128"
            "CODE_39" -> "Code 39"
            "CODABAR" -> "Codabar"
            "ITF" -> "ITF"
            "RSS_14" -> "RSS 14"
            "RSS_EXPANDED" -> "RSS Expanded"
            "MAXICODE" -> "MaxiCode"
            "RSS_14_TRAINEO" -> "RSS 14 Trained"
            else -> "Unknown"
        }
    }

    /**
     * Switch between scan and generate modes.
     */
    fun switchMode(mode: QrScannerMode) {
        uiState = uiState.copy(
            mode = mode,
            generatedQrBitmap = if (mode == QrScannerMode.SCAN) null else uiState.generatedQrBitmap
        )
    }

    /**
     * Process a detected barcode. Adds to results if not a duplicate.
     */
    fun onBarcodeDetected(value: String, format: String) {
        if (value.isNotBlank() && !_scanResults.contains(value)) {
            _scanResults.add(value)
            uiState = uiState.copy(scanResults = _scanResults.toList())
        }
    }

    /**
     * Clear all scan results.
     */
    fun clearResults() {
        _scanResults.clear()
        uiState = uiState.copy(scanResults = emptyList())
    }

    /**
     * Update the text input for QR code generation.
     */
    fun onQrInputChanged(text: String) {
        qrInput = text
    }

    /**
     * Generate a QR code bitmap from the current input text.
     * Returns null if input is blank.
     */
    fun generateQrBitmap(size: Int = 512): Bitmap? {
        val matrix = encodeQrMatrix(qrInput, size)
        return if (matrix != null) {
            val bitmap = qrMatrixToBitmap(matrix, size)
            uiState = uiState.copy(generatedQrBitmap = bitmap)
            bitmap
        } else {
            uiState = uiState.copy(generatedQrBitmap = null)
            null
        }
    }
}
