package com.armyknife.tools.features.utility

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.random.Random
import java.util.UUID

/**
 * Random generation types.
 */
enum class RandomType(val label: String) {
    NUMBER("Random Number"),
    STRING("Random String"),
    UUID("UUID"),
    DICE("Dice Roll"),
    COIN("Coin Flip")
}

/**
 * ViewModel for the Random Generator tool.
 */
class RandomGeneratorViewModel : ViewModel() {

    var randomType: RandomType = RandomType.NUMBER
        internal set

    var min: Int = 1
        private set

    var max: Int = 100
        private set

    var stringLength: Int = 1
        private set

    var result: String by mutableStateOf("")
        private set

    companion object {
        private const val ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    }

    // ── Type ──

    fun cycleType() {
        randomType = when (randomType) {
            RandomType.NUMBER -> RandomType.STRING
            RandomType.STRING -> RandomType.UUID
            RandomType.UUID -> RandomType.DICE
            RandomType.DICE -> RandomType.COIN
            RandomType.COIN -> RandomType.NUMBER
        }
    }

    // ── Min/Max ──

    fun updateMin(value: Int) {
        min = value
    }

    fun updateMax(value: Int) {
        max = value
    }

    // ── String length ──

    fun updateStringLength(value: Int) {
        stringLength = value.coerceIn(1, 64)
    }

    // ── Number ──

    fun generateRandomInt(): Int {
        val lo = minOf(min, max)
        val hi = maxOf(min, max)
        val value = Random.nextInt(lo, hi + 1)
        result = value.toString()
        return value
    }

    // ── String ──

    fun generateRandomString(): String {
        val len = stringLength.coerceIn(1, 64)
        val chars = (1..len).map { ALPHANUMERIC.random(Random) }.joinToString("")
        result = chars
        return chars
    }

    // ── UUID ──

    fun generateUuid(): String {
        val uuid = UUID.randomUUID().toString()
        result = uuid
        return uuid
    }

    // ── Dice ──

    fun rollDice(): Int {
        val value = Random.nextInt(1, 7)
        result = value.toString()
        return value
    }

    // ── Coin ──

    fun flipCoin(): String {
        val value = if (Random.nextBoolean()) "HEADS" else "TAILS"
        result = value
        return value
    }

    // ── Reset ──

    fun resetToDefaults() {
        randomType = RandomType.NUMBER
        min = 1
        max = 100
        stringLength = 1
        result = ""
    }
}
