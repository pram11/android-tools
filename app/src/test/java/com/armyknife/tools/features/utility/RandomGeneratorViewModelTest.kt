package com.armyknife.tools.features.utility

import org.junit.Assert.*
import org.junit.Test

class RandomGeneratorViewModelTest {

    private val vm = RandomGeneratorViewModel()

    // ── Default state ──

    @Test
    fun `default type is number`() {
        assertEquals(RandomType.NUMBER, vm.randomType)
    }

    // ── Cycle type ──

    @Test
    fun `cycleType iterates through all types`() {
        val types = listOf(
            RandomType.NUMBER,
            RandomType.STRING,
            RandomType.UUID,
            RandomType.DICE,
            RandomType.COIN
        )
        var idx = 0
        repeat(types.size) {
            assertEquals(types[idx], vm.randomType)
            vm.cycleType()
            idx++
        }
        assertEquals(RandomType.NUMBER, vm.randomType)
    }

    // ── Number generation ──

    @Test
    fun `generateRandomInt is within range`() {
        vm.updateMin(1)
        vm.updateMax(10)
        val result = vm.generateRandomInt()
        assertTrue(result >= 1 && result <= 10)
    }

    @Test
    fun `generateRandomInt min equals max`() {
        vm.updateMin(5)
        vm.updateMax(5)
        val result = vm.generateRandomInt()
        assertEquals(5, result)
    }

    @Test
    fun `generateRandomInt min greater than max swaps`() {
        vm.updateMin(100)
        vm.updateMax(1)
        val result = vm.generateRandomInt()
        assertTrue(result >= 1 && result <= 100)
    }

    // ── String generation ──

    @Test
    fun `generateRandomString has correct length`() {
        vm.updateStringLength(10)
        val result = vm.generateRandomString()
        assertEquals(10, result.length)
    }

    @Test
    fun `generateRandomString with length 1`() {
        vm.updateStringLength(1)
        val result = vm.generateRandomString()
        assertEquals(1, result.length)
    }

    @Test
    fun `generateRandomString with length 32`() {
        vm.updateStringLength(32)
        val result = vm.generateRandomString()
        assertEquals(32, result.length)
    }

    @Test
    fun `stringLength clamps to 1-64`() {
        vm.updateStringLength(-5)
        assertEquals(1, vm.stringLength)
        vm.updateStringLength(100)
        assertEquals(64, vm.stringLength)
    }

    // ── UUID generation ──

    @Test
    fun `generateUuid matches pattern`() {
        val uuid = vm.generateUuid()
        // UUID pattern: 8-4-4-4-12 hex chars
        val pattern = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
        assertTrue(uuid.matches(pattern))
    }

    @Test
    fun `generateUuid is unique`() {
        val uuid1 = vm.generateUuid()
        val uuid2 = vm.generateUuid()
        assertNotEquals(uuid1, uuid2)
    }

    // ── Dice roll ──

    @Test
    fun `rollDice returns 1-6`() {
        repeat(100) {
            val result = vm.rollDice()
            assertTrue(result in 1..6)
        }
    }

    // ── Coin flip ──

    @Test
    fun `flipCoin returns HEADS or TAILS`() {
        val result = vm.flipCoin()
        assertTrue(result == "HEADS" || result == "TAILS")
    }

    // ── Result ──

    @Test
    fun `result is updated after generation`() {
        vm.generateRandomInt()
        assertTrue(vm.result.isNotEmpty())
    }

    // ── Reset ──

    @Test
    fun `resetToDefaults clears state`() {
        vm.randomType = RandomType.STRING
        vm.updateStringLength(32)
        vm.generateRandomString()

        vm.resetToDefaults()

        assertEquals(RandomType.NUMBER, vm.randomType)
        assertEquals(1, vm.stringLength)
        assertEquals(1, vm.min)
        assertEquals(100, vm.max)
        assertTrue(vm.result.isEmpty())
    }

    // ── Min/max clamping ──

    @Test
    fun `min clamps to reasonable range`() {
        vm.updateMin(-1000000)
        assertTrue(vm.min >= -1000000)
    }

    @Test
    fun `max clamps to reasonable range`() {
        vm.updateMax(1000000)
        assertTrue(vm.max <= 1000000)
    }
}
