package com.armyknife.tools.features.utility

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Morse code conversion mode.
 */
enum class MorseMode(val label: String) {
    TEXT_TO_MORSE("Text → Morse"),
    MORSE_TO_TEXT("Morse → Text")
}

/**
 * ViewModel for the Morse Code Converter tool.
 */
class MorseCodeViewModel : ViewModel() {

    var mode: MorseMode = MorseMode.TEXT_TO_MORSE
        internal set

    var input: String by mutableStateOf("")
        private set

    var output: String by mutableStateOf("")
        private set

    companion object {
        private val TEXT_TO_MORSE_MAP = mapOf(
            'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..",
            'E' to ".", 'F' to "..-.", 'G' to "--.", 'H' to "....",
            'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..",
            'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.",
            'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
            'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
            'Y' to "-.--", 'Z' to "--..",
            '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--",
            '4' to "....-", '5' to ".....", '6' to "-....", '7' to "--...",
            '8' to "---..", '9' to "----.",
        )

        private val MORSE_TO_TEXT_MAP = TEXT_TO_MORSE_MAP.entries.associateBy({ it.value }, { it.key })
    }

    // ── Mode ──

    fun toggleMode() {
        mode = if (mode == MorseMode.TEXT_TO_MORSE) MorseMode.MORSE_TO_TEXT else MorseMode.TEXT_TO_MORSE
    }

    // ── Input ──

    fun updateInput(value: String) {
        input = value
        if (value.isNotEmpty()) {
            process()
        } else {
            output = ""
        }
    }

    // ── Encoding ──

    fun encodeToMorse(): String {
        return input.uppercase()
            .map { ch ->
                when {
                    ch.isLetterOrDigit() -> TEXT_TO_MORSE_MAP[ch] ?: ""
                    ch == ' ' -> "/"
                    else -> ""
                }
            }
            .filter { it.isNotEmpty() }
            .joinToString(" ")
    }

    // ── Decoding ──

    fun decodeFromMorse(): String {
        return input.split(" ")
            .map { code ->
                when {
                    code.isEmpty() -> ""
                    code == "/" -> " "
                    else -> MORSE_TO_TEXT_MAP[code] ?: "?"
                }
            }
            .joinToString("")
    }

    // ── Process ──

    fun process(): String {
        output = when (mode) {
            MorseMode.TEXT_TO_MORSE -> encodeToMorse()
            MorseMode.MORSE_TO_TEXT -> decodeFromMorse()
        }
        return output
    }

    // ── Reset ──

    fun resetToDefaults() {
        mode = MorseMode.TEXT_TO_MORSE
        input = ""
        output = ""
    }
}
