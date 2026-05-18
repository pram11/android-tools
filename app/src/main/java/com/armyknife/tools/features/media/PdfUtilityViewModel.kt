package com.armyknife.tools.features.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Supported page sizes for PDF generation.
 * Dimensions in points (72 points = 1 inch).
 */
enum class PdfPageSize(
    val widthPoints: Int,
    val heightPoints: Int,
    val label: String
) {
    A4(595, 842, "A4"),
    LETTER(612, 792, "Letter"),
    LEGAL(612, 1008, "Legal")
}

/**
 * ViewModel for the PDF Utility tool.
 * Manages page size, font size, margins, line spacing, metadata, and text input.
 * Provides page-count estimation and filename generation.
 */
class PdfUtilityViewModel : ViewModel() {

    var pageSize: PdfPageSize = PdfPageSize.A4
        internal set

    var fontSize: Int = 12
        private set

    var marginTop: Float by mutableFloatStateOf(72f)
        private set

    var marginBottom: Float by mutableFloatStateOf(72f)
        private set

    var marginLeft: Float by mutableFloatStateOf(72f)
        private set

    var marginRight: Float by mutableFloatStateOf(72f)
        private set

    var lineSpacing: Float = 1.5f
        private set

    var documentTitle: String by mutableStateOf("")
        private set

    var documentAuthor: String by mutableStateOf("")
        private set

    var text: String by mutableStateOf("")
        private set

    companion object {
        private const val FONT_MIN = 8
        private const val FONT_MAX = 72
        private const val MARGIN_MIN = 0f
        private const val MARGIN_MAX = 200f
        private const val LINE_SPACING_MIN = 1.0f
        private const val LINE_SPACING_MAX = 3.0f
        /**
         * Proportional font character-width factor.
         * Average character width ≈ 0.6 × em-size for proportional fonts.
         */
        private const val CHAR_WIDTH_FACTOR = 0.6f
    }

    // ── Page size ──

    fun cyclePageSize() {
        pageSize = when (pageSize) {
            PdfPageSize.A4 -> PdfPageSize.LETTER
            PdfPageSize.LETTER -> PdfPageSize.LEGAL
            PdfPageSize.LEGAL -> PdfPageSize.A4
        }
    }

    // ── Font size ──

    fun updateFontSize(value: Int) {
        fontSize = value.coerceIn(FONT_MIN, FONT_MAX)
    }

    // ── Margins ──

    fun updateMargins(top: Float, bottom: Float, left: Float, right: Float) {
        marginTop = top.coerceIn(MARGIN_MIN, MARGIN_MAX)
        marginBottom = bottom.coerceIn(MARGIN_MIN, MARGIN_MAX)
        marginLeft = left.coerceIn(MARGIN_MIN, MARGIN_MAX)
        marginRight = right.coerceIn(MARGIN_MIN, MARGIN_MAX)
    }

    // ── Line spacing ──

    fun updateLineSpacing(value: Float) {
        lineSpacing = value.coerceIn(LINE_SPACING_MIN, LINE_SPACING_MAX)
    }

    // ── Metadata ──

    fun updateDocumentTitle(title: String) {
        documentTitle = title
    }

    fun updateDocumentAuthor(author: String) {
        documentAuthor = author
    }

    // ── Text input ──

    fun updateText(value: String) {
        text = value
    }

    // ── Line height ──

    /**
     * Line height in points = fontSize × lineSpacing multiplier.
     */
    fun calculateLineHeight(): Float = fontSize.toFloat() * lineSpacing

    // ── Usable area ──

    fun calculateUsableWidth(): Float = pageSize.widthPoints.toFloat() - marginLeft - marginRight

    fun calculateUsableHeight(): Float = pageSize.heightPoints.toFloat() - marginTop - marginBottom

    // ── Chars per line ──

    /**
     * Estimate how many characters fit on one line.
     * usableWidth / (fontSize × CHAR_WIDTH_FACTOR)
     */
    fun estimateCharsPerLine(): Int {
        val usableWidth = calculateUsableWidth()
        val avgCharWidth = fontSize.toFloat() * CHAR_WIDTH_FACTOR
        return (usableWidth / avgCharWidth).toInt().coerceAtLeast(1)
    }

    // ── Lines per page ──

    fun estimateLinesPerPage(): Int {
        val usableHeight = calculateUsableHeight()
        val lineHeight = calculateLineHeight()
        return (usableHeight / lineHeight).toInt().coerceAtLeast(1)
    }

    // ── Page count ──

    /**
     * Estimate total pages needed for current text given current settings.
     */
    fun estimatePageCount(textInput: String): Int {
        if (textInput.isBlank()) return 0
        val charsPerLine = estimateCharsPerLine()
        val linesPerPage = estimateLinesPerPage()
        val totalLines = estimateTotalLines(textInput, charsPerLine)
        return (totalLines.toFloat() / linesPerPage).toInt() + 1
    }

    /**
     * Estimate total lines from text using charsPerLine.
     */
    private fun estimateTotalLines(textInput: String, charsPerLine: Int): Int {
        val lines = textInput.split("\n")
        var totalLines = 0
        for (line in lines) {
            if (line.isEmpty()) {
                totalLines++
            } else {
                val wrapped = (line.length.toFloat() / charsPerLine).toInt() + 1
                totalLines += wrapped
            }
        }
        return totalLines
    }

    // ── Filename ──

    fun generateFilename(baseName: String): String {
        val clean = if (baseName.lowercase().endsWith(".pdf")) {
            baseName.substring(0, baseName.length - 4)
        } else {
            baseName
        }
        return "$clean.pdf"
    }

    // ── Reset ──

    fun resetToDefaults() {
        pageSize = PdfPageSize.A4
        fontSize = 12
        marginTop = 72f
        marginBottom = 72f
        marginLeft = 72f
        marginRight = 72f
        lineSpacing = 1.5f
        documentTitle = ""
        documentAuthor = ""
        text = ""
    }
}
