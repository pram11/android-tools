package com.armyknife.tools.features.utility

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

/**
 * Cryptographic/encoding operations supported.
 */
enum class CryptoOp(val label: String) {
    BASE64_ENCODE("Base64 Encode"),
    BASE64_DECODE("Base64 Decode"),
    HEX_ENCODE("Hex Encode"),
    HEX_DECODE("Hex Decode"),
    URL_ENCODE("URL Encode"),
    URL_DECODE("URL Decode"),
    ROT13("ROT13"),
    REVERSE("Reverse")
}

/**
 * ViewModel for the Text Crypto tool.
 * Manages operation selection, input text, and processing.
 */
class TextCryptoViewModel : ViewModel() {

    var operation: CryptoOp = CryptoOp.BASE64_ENCODE
        internal set

    var input: String by mutableStateOf("")
        private set

    var output: String by mutableStateOf("")
        private set

    var hasError: Boolean by mutableStateOf(false)
        private set

    // ── Operation ──

    fun cycleOperation() {
        val ops = CryptoOp.entries.toTypedArray()
        val currentIdx = ops.indexOf(operation)
        val nextIdx = (currentIdx + 1) % ops.size
        operation = ops[nextIdx]
    }

    // ── Input ──

    fun updateInput(value: String) {
        input = value
        // Auto-process if input is not empty
        if (value.isNotEmpty()) {
            process()
        } else {
            output = ""
            hasError = false
        }
    }

    // ── Processing ──

    fun process(): String {
        if (input.isEmpty()) {
            output = ""
            hasError = false
            return ""
        }

        var result = ""
        return try {
            result = when (operation) {
                CryptoOp.BASE64_ENCODE -> java.util.Base64.getEncoder().encodeToString(input.toByteArray())
                CryptoOp.BASE64_DECODE -> String(java.util.Base64.getDecoder().decode(input))
                CryptoOp.HEX_ENCODE -> input.toByteArray().joinToString("") { "%02x".format(it) }
                CryptoOp.HEX_DECODE -> hexStringToString(input)
                CryptoOp.URL_ENCODE -> URLEncoder.encode(input, "UTF-8")
                CryptoOp.URL_DECODE -> URLDecoder.decode(input, "UTF-8")
                CryptoOp.ROT13 -> rot13(input)
                CryptoOp.REVERSE -> input.reversed()
            }
            hasError = false
            result
        } catch (e: Exception) {
            hasError = true
            "Error: ${e.message ?: "Invalid input"}"
        }.also { output = it }
    }

    private fun hexStringToString(hex: String): String {
        if (hex.length % 2 != 0) throw IllegalArgumentException("Hex string must have even length")
        val bytes = ByteArray(hex.length / 2)
        for (i in bytes.indices) {
            bytes[i] = hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return String(bytes, Charsets.UTF_8)
    }

    private fun rot13(text: String): String {
        return text.map { c ->
            when (c) {
                in 'a'..'z' -> ('a' + (c - 'a' + 13) % 26).toChar()
                in 'A'..'Z' -> ('A' + (c - 'A' + 13) % 26).toChar()
                else -> c
            }
        }.joinToString("")
    }

    // ── Reset ──

    fun resetToDefaults() {
        operation = CryptoOp.BASE64_ENCODE
        input = ""
        output = ""
        hasError = false
    }
}
