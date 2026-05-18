package com.armyknife.tools.features.utility

import org.junit.Assert.*
import org.junit.Test

class TextCryptoViewModelTest {

    private val vm = TextCryptoViewModel()

    // ── Default state ──

    @Test
    fun `default operation is Base64 encode`() {
        assertEquals(CryptoOp.BASE64_ENCODE, vm.operation)
    }

    @Test
    fun `default input is empty`() {
        assertTrue(vm.input.isEmpty())
    }

    // ── Base64 ──

    @Test
    fun `Base64 encode`() {
        vm.operation = CryptoOp.BASE64_ENCODE
        vm.updateInput("Hello")
        val result = vm.process()
        assertEquals("SGVsbG8=", result)
    }

    @Test
    fun `Base64 decode`() {
        vm.operation = CryptoOp.BASE64_DECODE
        vm.updateInput("SGVsbG8=")
        val result = vm.process()
        assertEquals("Hello", result)
    }

    @Test
    fun `Base64 decode invalid returns error`() {
        vm.operation = CryptoOp.BASE64_DECODE
        vm.updateInput("not-valid-base64!!!")
        val result = vm.process()
        assertTrue(result.startsWith("Error"))
    }

    // ── Hex ──

    @Test
    fun `Hex encode`() {
        vm.operation = CryptoOp.HEX_ENCODE
        vm.updateInput("Hello")
        val result = vm.process()
        assertEquals("48656c6c6f", result)
    }

    @Test
    fun `Hex decode`() {
        vm.operation = CryptoOp.HEX_DECODE
        vm.updateInput("48656c6c6f")
        val result = vm.process()
        assertEquals("Hello", result)
    }

    @Test
    fun `Hex decode odd length returns error`() {
        vm.operation = CryptoOp.HEX_DECODE
        vm.updateInput("48656c6c6")
        val result = vm.process()
        assertTrue(result.startsWith("Error"))
    }

    // ── URL Encode/Decode ──

    @Test
    fun `URL encode`() {
        vm.operation = CryptoOp.URL_ENCODE
        vm.updateInput("hello world & foo=bar")
        val result = vm.process()
        // URLEncoder encodes space as + or %20, & as %26
        assertTrue(result.contains("%26") || result.contains("+"))
    }

    @Test
    fun `URL decode`() {
        vm.operation = CryptoOp.URL_DECODE
        vm.updateInput("hello%20world")
        val result = vm.process()
        assertEquals("hello world", result)
    }

    // ── Reverse ──

    @Test
    fun `Reverse string`() {
        vm.operation = CryptoOp.REVERSE
        vm.updateInput("Hello")
        val result = vm.process()
        assertEquals("olleH", result)
    }

    // ── ROT13 ──

    @Test
    fun `ROT13 encode`() {
        vm.operation = CryptoOp.ROT13
        vm.updateInput("Hello")
        val result = vm.process()
        assertEquals("Uryyb", result)
    }

    @Test
    fun `ROT13 double application returns original`() {
        vm.operation = CryptoOp.ROT13
        vm.updateInput("Hello World")
        val once = vm.process()
        vm.updateInput(once)
        val twice = vm.process()
        assertEquals("Hello World", twice)
    }

    // ── Operation cycling ──

    @Test
    fun `cycleOperation iterates through all`() {
        val count = CryptoOp.entries.size
        val start = vm.operation
        repeat(count) {
            vm.cycleOperation()
        }
        assertEquals(start, vm.operation)
    }

    // ── Empty input ──

    @Test
    fun `process empty input returns empty`() {
        vm.operation = CryptoOp.BASE64_ENCODE
        vm.updateInput("")
        val result = vm.process()
        assertTrue(result.isEmpty())
    }

    // ── Reset ──

    @Test
    fun `resetToDefaults clears state`() {
        vm.operation = CryptoOp.HEX_ENCODE
        vm.updateInput("test")

        vm.resetToDefaults()

        assertEquals(CryptoOp.BASE64_ENCODE, vm.operation)
        assertTrue(vm.input.isEmpty())
    }
}
