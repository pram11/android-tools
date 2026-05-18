package com.armyknife.tools.features.hardware

import org.junit.Assert.*
import org.junit.Test

class QrScannerViewModelTest {

    private val vm = QrScannerViewModel()

    @Test
    fun `initial state - scan mode active and no results`() {
        assertEquals(QrScannerMode.SCAN, vm.uiState.mode)
        assertTrue(vm.uiState.scanResults.isEmpty())
        assertNull(vm.uiState.generatedQrBitmap)
    }

    @Test
    fun `switch to generate mode updates uiState`() {
        vm.switchMode(QrScannerMode.GENERATE)
        assertEquals(QrScannerMode.GENERATE, vm.uiState.mode)
    }

    @Test
    fun `switch to scan mode updates uiState`() {
        vm.switchMode(QrScannerMode.SCAN)
        assertEquals(QrScannerMode.SCAN, vm.uiState.mode)
    }

    @Test
    fun `add scan result appends to list`() {
        vm.onBarcodeDetected("https://example.com", "QR_CODE")
        vm.onBarcodeDetected("Hello World", "QR_CODE")

        assertEquals(2, vm.uiState.scanResults.size)
        assertEquals("https://example.com", vm.uiState.scanResults[0])
        assertEquals("Hello World", vm.uiState.scanResults[1])
    }

    @Test
    fun `duplicate scan result is not added`() {
        vm.onBarcodeDetected("same-value", "QR_CODE")
        vm.onBarcodeDetected("same-value", "QR_CODE")

        assertEquals(1, vm.uiState.scanResults.size)
    }

    @Test
    fun `clear scan results empties the list`() {
        vm.onBarcodeDetected("test1", "QR_CODE")
        vm.onBarcodeDetected("test2", "QR_CODE")
        vm.clearResults()

        assertTrue(vm.uiState.scanResults.isEmpty())
    }

    @Test
    fun `encode qr matrix for valid text produces matrix`() {
        val matrix = encodeQrMatrix("https://example.com", 128)
        assertNotNull("QR matrix should not be null for valid input", matrix)
        assertEquals(128 * 128, matrix?.size)
    }

    @Test
    fun `encode qr matrix for empty text produces null`() {
        val matrix = encodeQrMatrix("")
        assertNull("QR matrix should be null for empty input", matrix)
    }

    @Test
    fun `encode qr matrix for blank whitespace produces null`() {
        val matrix = encodeQrMatrix("   ")
        assertNull("QR matrix should be null for blank input", matrix)
    }

    @Test
    fun `qr input update sets the text`() {
        vm.onQrInputChanged("test-data")
        assertEquals("test-data", vm.qrInput)
    }

    // Bitmap generation requires Android runtime — tested via matrix encoding above

    @Test
    fun `format label maps QR_CODE correctly`() {
        assertEquals("QR Code", QrScannerViewModel.formatLabel("QR_CODE"))
    }

    @Test
    fun `format label maps DATA_MATRIX correctly`() {
        assertEquals("Data Matrix", QrScannerViewModel.formatLabel("DATA_MATRIX"))
    }

    @Test
    fun `format label maps UNKNOWN correctly`() {
        assertEquals("Unknown", QrScannerViewModel.formatLabel("UNKNOWN_TYPE"))
    }

    @Test
    fun `format label maps PDF_417 correctly`() {
        assertEquals("PDF 417", QrScannerViewModel.formatLabel("PDF_417"))
    }

    @Test
    fun `format label maps AZTEC correctly`() {
        assertEquals("Aztec", QrScannerViewModel.formatLabel("AZTEC"))
    }

    @Test
    fun `format label maps UPC_A correctly`() {
        assertEquals("UPC-A", QrScannerViewModel.formatLabel("UPC_A"))
    }

    @Test
    fun `format label maps EAN_13 correctly`() {
        assertEquals("EAN-13", QrScannerViewModel.formatLabel("EAN_13"))
    }
}
