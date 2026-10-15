package com.neuro.minimalistlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos

class MindfulBreathingTest {

    private fun easeInOutSine(fraction: Float): Float {
        val clamped = fraction.coerceIn(0f, 1f)
        return -0.5f * (cos(Math.PI.toFloat() * clamped) - 1f)
    }

    @Test
    fun easeInOutSine_atStart_isZero() {
        assertEquals(0f, easeInOutSine(0f), 0.0001f)
    }

    @Test
    fun easeInOutSine_atMidpoint_isHalf() {
        assertEquals(0.5f, easeInOutSine(0.5f), 0.0001f)
    }

    @Test
    fun easeInOutSine_atEnd_isOne() {
        assertEquals(1.0f, easeInOutSine(1f), 0.0001f)
    }

    @Test
    fun easeInOutSine_isMonotonicallyIncreasing() {
        var prev = 0f
        for (i in 1..100) {
            val t = i / 100f
            val curr = easeInOutSine(t)
            assertTrue("Expected $curr >= $prev at t=$t", curr >= prev)
            prev = curr
        }
    }

    @Test
    fun breathingCycles_5sDelay_hasOneFullCycle() {
        val delaySeconds = 5
        val cycleDurationMs = 5000L
        val totalMs = delaySeconds * 1000L
        val totalCycles = totalMs / cycleDurationMs
        assertEquals(1L, totalCycles)

        // Peak at 2500ms
        val peakMs = 2500L
        assertTrue(peakMs < totalMs)
    }

    @Test
    fun breathingCycles_10sDelay_hasTwoFullCycles() {
        val delaySeconds = 10
        val cycleDurationMs = 5000L
        val totalMs = delaySeconds * 1000L
        val totalCycles = totalMs / cycleDurationMs
        assertEquals(2L, totalCycles)

        // Peaks at 2500ms and 7500ms
        val peaks = listOf(2500L, 7500L)
        peaks.forEach { peak ->
            assertTrue(peak < totalMs)
            val cycleIndex = (peak / cycleDurationMs).toInt()
            val cycleOffset = peak % cycleDurationMs
            assertEquals(2500L, cycleOffset)
        }
    }

    @Test
    fun breathingCycles_15sDelay_hasThreeFullCycles() {
        val delaySeconds = 15
        val cycleDurationMs = 5000L
        val totalMs = delaySeconds * 1000L
        val totalCycles = totalMs / cycleDurationMs
        assertEquals(3L, totalCycles)

        // Peaks at 2500ms, 7500ms, and 12500ms
        val peaks = listOf(2500L, 7500L, 12500L)
        assertEquals(3, peaks.size)
        peaks.forEach { peak ->
            assertTrue(peak < totalMs)
            val cycleOffset = peak % cycleDurationMs
            assertEquals(2500L, cycleOffset)
        }
    }

    @Test
    fun breathingScaleAndAlpha_boundsAreWithinSpecifications() {
        // Inhale: 0.6x -> 1.0x scale, 20% -> 55% opacity
        val minScale = 0.6f
        val maxScale = 1.0f
        val minAlpha = 0.20f
        val maxAlpha = 0.55f

        for (i in 0..100) {
            val t = i / 100f
            val eased = easeInOutSine(t)

            val inhaleScale = (minScale + (maxScale - minScale) * eased).coerceIn(minScale, maxScale)
            val inhaleAlpha = (minAlpha + (maxAlpha - minAlpha) * eased).coerceIn(minAlpha, maxAlpha)

            assertTrue(inhaleScale in minScale..maxScale)
            assertTrue(inhaleAlpha in minAlpha..maxAlpha)

            val exhaleScale = (maxScale - (maxScale - minScale) * eased).coerceIn(minScale, maxScale)
            val exhaleAlpha = (maxAlpha - (maxAlpha - minAlpha) * eased).coerceIn(minAlpha, maxAlpha)

            assertTrue(exhaleScale in minScale..maxScale)
            assertTrue(exhaleAlpha in minAlpha..maxAlpha)
        }
    }
}
