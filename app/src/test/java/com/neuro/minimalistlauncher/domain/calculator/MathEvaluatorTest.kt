package com.neuro.minimalistlauncher.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MathEvaluatorTest {

    @Test
    fun evaluate_multiplication_returnsCorrectResult() {
        assertEquals("540", MathEvaluator.evaluate("45 * 12"))
        assertEquals("540", MathEvaluator.evaluate("45 × 12"))
        assertEquals("540", MathEvaluator.evaluate("45x12"))
    }

    @Test
    fun evaluate_additionAndSubtraction_returnsCorrectResult() {
        assertEquals("35", MathEvaluator.evaluate("10 + 25"))
        assertEquals("7", MathEvaluator.evaluate("15 - 8"))
    }

    @Test
    fun evaluate_division_returnsCorrectResult() {
        assertEquals("25", MathEvaluator.evaluate("100 / 4"))
        assertEquals("25", MathEvaluator.evaluate("100 ÷ 4"))
        assertEquals("2.5", MathEvaluator.evaluate("5 / 2"))
    }

    @Test
    fun evaluate_orderOfOperationsAndParentheses() {
        assertEquals("14", MathEvaluator.evaluate("2 + 3 * 4"))
        assertEquals("20", MathEvaluator.evaluate("(2 + 3) * 4"))
        assertEquals("8", MathEvaluator.evaluate("2 ^ 3"))
    }

    @Test
    fun evaluate_modulo() {
        assertEquals("1", MathEvaluator.evaluate("10 % 3"))
    }

    @Test
    fun evaluate_divisionByZero_returnsCannotDivideByZero() {
        assertEquals("Cannot divide by 0", MathEvaluator.evaluate("42 / 0"))
    }

    @Test
    fun evaluate_normalWords_returnsNull() {
        assertNull(MathEvaluator.evaluate("camera"))
        assertNull(MathEvaluator.evaluate("phone"))
        assertNull(MathEvaluator.evaluate("whatsapp"))
        assertNull(MathEvaluator.evaluate(""))
        assertNull(MathEvaluator.evaluate("   "))
    }
}
