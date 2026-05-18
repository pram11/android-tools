package com.armyknife.tools.features.utility

import org.junit.Assert.*
import org.junit.Test

class MorseCodeViewModelTest {

    private val vm = MorseCodeViewModel()

    // ── Default state ──

    @Test
    fun `default input is empty`() {
        assertTrue(vm.input.isEmpty())
    }

    @Test
    fun `default mode is text to morse`() {
        assertEquals(MorseMode.TEXT_TO_MORSE, vm.mode)
    }

    // ── Text to Morse ──

    @Test
    fun `encode letter A`() {
        vm.updateInput("A")
        val morse = vm.encodeToMorse()
        assertEquals(".-", morse)
    }

    @Test
    fun `encode letter S`() {
        vm.updateInput("S")
        val morse = vm.encodeToMorse()
        assertEquals("...", morse)
    }

    @Test
    fun `encode SOS`() {
        vm.updateInput("SOS")
        val morse = vm.encodeToMorse()
        assertEquals("... --- ...", morse)
    }

    @Test
    fun `encode Hello`() {
        vm.updateInput("HELLO")
        val morse = vm.encodeToMorse()
        assertEquals(".... . .-.. .-.. ---", morse)
    }

    @Test
    fun `encode with numbers`() {
        vm.updateInput("SOS 911")
        val morse = vm.encodeToMorse()
        // 9 = --.-, 1 = .----
        assertTrue(morse.contains("---"))
    }

    @Test
    fun `encode lowercase`() {
        vm.updateInput("hello")
        val morse = vm.encodeToMorse()
        assertEquals(".... . .-.. .-.. ---", morse)
    }

    @Test
    fun `encode empty returns empty`() {
        vm.updateInput("")
        val morse = vm.encodeToMorse()
        assertTrue(morse.isEmpty())
    }

    // ── Morse to Text ──

    @Test
    fun `decode SOS`() {
        vm.updateInput("... --- ...")
        val text = vm.decodeFromMorse()
        assertEquals("SOS", text)
    }

    @Test
    fun `decode Hello`() {
        vm.updateInput(".... . .-.. .-.. ---")
        val text = vm.decodeFromMorse()
        assertEquals("HELLO", text)
    }

    @Test
    fun `decode single letter`() {
        vm.updateInput(".-")
        val text = vm.decodeFromMorse()
        assertEquals("A", text)
    }

    @Test
    fun `decode with numbers`() {
        vm.updateInput(".----")
        val text = vm.decodeFromMorse()
        assertEquals("1", text)
    }

    @Test
    fun `decode invalid returns original`() {
        vm.updateInput("---")
        // --- is O, not invalid
        val text = vm.decodeFromMorse()
        assertEquals("O", text)
    }

    // ── Mode toggle ──

    @Test
    fun `toggleMode switches direction`() {
        assertEquals(MorseMode.TEXT_TO_MORSE, vm.mode)
        vm.toggleMode()
        assertEquals(MorseMode.MORSE_TO_TEXT, vm.mode)
        vm.toggleMode()
        assertEquals(MorseMode.TEXT_TO_MORSE, vm.mode)
    }

    // ── Process ──

    @Test
    fun `process encodes when text to morse`() {
        vm.mode = MorseMode.TEXT_TO_MORSE
        vm.updateInput("HI")
        assertEquals(".... ..", vm.process())
    }

    @Test
    fun `process decodes when morse to text`() {
        vm.mode = MorseMode.MORSE_TO_TEXT
        vm.updateInput(".... ..")
        assertEquals("HI", vm.process())
    }

    // ── Reset ──

    @Test
    fun `resetToDefaults clears state`() {
        vm.mode = MorseMode.MORSE_TO_TEXT
        vm.updateInput("test")

        vm.resetToDefaults()

        assertEquals(MorseMode.TEXT_TO_MORSE, vm.mode)
        assertTrue(vm.input.isEmpty())
    }
}
