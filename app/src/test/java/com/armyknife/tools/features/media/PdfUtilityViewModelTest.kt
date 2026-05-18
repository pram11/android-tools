package com.armyknife.tools.features.media

import org.junit.Assert.*
import org.junit.Test

class PdfUtilityViewModelTest {

    private val vm = PdfUtilityViewModel()

    // ── PdfPageSize ──

    @Test
    fun `page size A4 has correct dimensions in points`() {
        val a4 = PdfPageSize.A4
        assertEquals(595, a4.widthPoints)
        assertEquals(842, a4.heightPoints)
    }

    @Test
    fun `page size LETTER has correct dimensions in points`() {
        val letter = PdfPageSize.LETTER
        assertEquals(612, letter.widthPoints)
        assertEquals(792, letter.heightPoints)
    }

    @Test
    fun `page size LEGAL has correct dimensions in points`() {
        val legal = PdfPageSize.LEGAL
        assertEquals(612, legal.widthPoints)
        assertEquals(1008, legal.heightPoints)
    }

    @Test
    fun `default page size is A4`() {
        assertEquals(PdfPageSize.A4, vm.pageSize)
    }

    @Test
    fun `cyclePageSize iterates A4 to LETTER to LEGAL to A4`() {
        assertEquals(PdfPageSize.A4, vm.pageSize)
        vm.cyclePageSize()
        assertEquals(PdfPageSize.LETTER, vm.pageSize)
        vm.cyclePageSize()
        assertEquals(PdfPageSize.LEGAL, vm.pageSize)
        vm.cyclePageSize()
        assertEquals(PdfPageSize.A4, vm.pageSize)
    }

    // ── Font size ──

    @Test
    fun `default fontSize is 12`() {
        assertEquals(12, vm.fontSize)
    }

    @Test
    fun `fontSize clamps to 8-72`() {
        vm.updateFontSize(5)
        assertEquals(8, vm.fontSize)
        vm.updateFontSize(100)
        assertEquals(72, vm.fontSize)
        vm.updateFontSize(16)
        assertEquals(16, vm.fontSize)
    }

    // ── Margins ──

    @Test
    fun `default margins are 72 points`() {
        assertEquals(72f, vm.marginTop, 0.01f)
        assertEquals(72f, vm.marginBottom, 0.01f)
        assertEquals(72f, vm.marginLeft, 0.01f)
        assertEquals(72f, vm.marginRight, 0.01f)
    }

    @Test
    fun `update margins clamps to 0-200`() {
        vm.updateMargins(-10f, 300f, 50f, 50f)
        assertEquals(0f, vm.marginTop, 0.01f)
        assertEquals(200f, vm.marginBottom, 0.01f)
        assertEquals(50f, vm.marginLeft, 0.01f)
        assertEquals(50f, vm.marginRight, 0.01f)
    }

    // ── Line spacing ──

    @Test
    fun `default line spacing multiplier is 1x5`() {
        assertEquals(1.5f, vm.lineSpacing, 0.01f)
    }

    @Test
    fun `line spacing clamps to 1x0 to 3x0`() {
        vm.updateLineSpacing(0.5f)
        assertEquals(1.0f, vm.lineSpacing, 0.01f)
        vm.updateLineSpacing(4.0f)
        assertEquals(3.0f, vm.lineSpacing, 0.01f)
        vm.updateLineSpacing(2.0f)
        assertEquals(2.0f, vm.lineSpacing, 0.01f)
    }

    // ── Metadata ──

    @Test
    fun `default metadata is empty`() {
        assertTrue(vm.documentTitle.isEmpty())
        assertTrue(vm.documentAuthor.isEmpty())
    }

    @Test
    fun `update document title`() {
        vm.updateDocumentTitle("My Report")
        assertEquals("My Report", vm.documentTitle)
    }

    @Test
    fun `update document author`() {
        vm.updateDocumentAuthor("John Doe")
        assertEquals("John Doe", vm.documentAuthor)
    }

    // ── Text input ──

    @Test
    fun `default text is empty`() {
        assertTrue(vm.text.isEmpty())
    }

    @Test
    fun `update text sets value`() {
        vm.updateText("Hello world")
        assertEquals("Hello world", vm.text)
    }

    // ── Page count estimation ──

    @Test
    fun `empty text yields 0 pages`() {
        assertEquals(0, vm.estimatePageCount(""))
    }

    @Test
    fun `empty text with spaces yields 0 pages`() {
        assertEquals(0, vm.estimatePageCount("   "))
    }

    @Test
    fun `short text yields at least 1 page`() {
        assertEquals(1, vm.estimatePageCount("Hello"))
    }

    @Test
    fun `page count increases with more text`() {
        val short = "Hello world"
        val long = "Hello world\n".repeat(500)
        val countShort = vm.estimatePageCount(short)
        val countLong = vm.estimatePageCount(long)
        assertTrue(countLong > countShort)
    }

    @Test
    fun `larger font size yields more pages`() {
        val text = "Hello world\n".repeat(100)
        vm.updateFontSize(10)
        val pagesSmall = vm.estimatePageCount(text)
        vm.updateFontSize(48)
        val pagesLarge = vm.estimatePageCount(text)
        assertTrue(pagesLarge > pagesSmall)
    }

    @Test
    fun `larger page size yields fewer pages`() {
        val text = "Hello world\n".repeat(100)
        vm.resetToDefaults()
        val pagesA4 = vm.estimatePageCount(text)
        vm.cyclePageSize() // A4→LETTER
        vm.cyclePageSize() // LETTER→LEGAL
        val pagesLegal = vm.estimatePageCount(text)
        assertTrue(pagesLegal <= pagesA4)
    }

    @Test
    fun `smaller margins yield fewer pages`() {
        val text = "Hello world\n".repeat(100)
        vm.updateMargins(72f, 72f, 72f, 72f)
        val pagesLargeMargins = vm.estimatePageCount(text)
        vm.updateMargins(0f, 0f, 0f, 0f)
        val pagesNoMargins = vm.estimatePageCount(text)
        assertTrue(pagesNoMargins <= pagesLargeMargins)
    }

    // ── Word wrapping estimation ──

    @Test
    fun `charsPerLine uses available width`() {
        vm.resetToDefaults()
        val charsPerLine = vm.estimateCharsPerLine()
        assertTrue(charsPerLine > 0)
        assertTrue(charsPerLine < 200)
    }

    @Test
    fun `charsPerLine decreases with larger font`() {
        vm.resetToDefaults()
        vm.updateFontSize(10)
        val wide = vm.estimateCharsPerLine()
        vm.updateFontSize(36)
        val narrow = vm.estimateCharsPerLine()
        assertTrue(narrow < wide)
    }

    @Test
    fun `charsPerLine decreases with larger margins`() {
        vm.resetToDefaults()
        vm.updateMargins(50f, 50f, 50f, 50f)
        val wide = vm.estimateCharsPerLine()
        vm.updateMargins(150f, 150f, 150f, 150f)
        val narrow = vm.estimateCharsPerLine()
        assertTrue(narrow < wide)
    }

    // ── Output filename ──

    @Test
    fun `generateFilename with default extension`() {
        assertEquals("document.pdf", vm.generateFilename("document"))
    }

    @Test
    fun `generateFilename strips existing pdf extension`() {
        assertEquals("document.pdf", vm.generateFilename("document.pdf"))
    }

    // ── Reset ──

    @Test
    fun `resetToDefaults clears all values`() {
        vm.cyclePageSize() // A4→LETTER
        vm.updateFontSize(48)
        vm.updateMargins(0f, 0f, 0f, 0f)
        vm.updateLineSpacing(3.0f)
        vm.updateDocumentTitle("Title")
        vm.updateDocumentAuthor("Author")
        vm.updateText("Some text")

        vm.resetToDefaults()

        assertEquals(PdfPageSize.A4, vm.pageSize)
        assertEquals(12, vm.fontSize)
        assertEquals(72f, vm.marginTop, 0.01f)
        assertEquals(72f, vm.marginBottom, 0.01f)
        assertEquals(72f, vm.marginLeft, 0.01f)
        assertEquals(72f, vm.marginRight, 0.01f)
        assertEquals(1.5f, vm.lineSpacing, 0.01f)
        assertTrue(vm.documentTitle.isEmpty())
        assertTrue(vm.documentAuthor.isEmpty())
        assertTrue(vm.text.isEmpty())
    }

    // ── Line height calculation ──

    @Test
    fun `lineHeight in points is fontSize * lineSpacing`() {
        vm.updateFontSize(14)
        vm.updateLineSpacing(1.5f)
        val lineHeight = vm.calculateLineHeight()
        assertEquals(21.0f, lineHeight, 0.01f)
    }

    @Test
    fun `lineHeight single spacing`() {
        vm.updateFontSize(16)
        vm.updateLineSpacing(1.0f)
        val lineHeight = vm.calculateLineHeight()
        assertEquals(16.0f, lineHeight, 0.01f)
    }

    // ── Usable area ──

    @Test
    fun `usableWidth is page width minus left and right margins`() {
        vm.resetToDefaults() // pageSize = A4
        vm.updateMargins(50f, 50f, 100f, 100f)
        val usable = vm.calculateUsableWidth()
        assertEquals(395f, usable, 0.01f) // 595 - 100 - 100
    }

    @Test
    fun `usableHeight is page height minus top and bottom margins`() {
        vm.resetToDefaults() // pageSize = A4
        vm.updateMargins(100f, 100f, 50f, 50f)
        val usable = vm.calculateUsableHeight()
        assertEquals(642f, usable, 0.01f) // 842 - 100 - 100
    }

    @Test
    fun `linesPerPage is usableHeight divided by lineHeight`() {
        vm.resetToDefaults()
        // default: A4, margins=72, fontSize=12, lineSpacing=1.5
        val lines = vm.estimateLinesPerPage()
        // usable height = 842-72-72 = 698, line height = 18
        // 698/18 ≈ 38
        assertTrue(lines > 30)
        assertTrue(lines < 50)
    }
}
