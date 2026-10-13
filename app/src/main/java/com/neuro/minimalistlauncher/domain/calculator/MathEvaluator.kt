package com.neuro.minimalistlauncher.domain.calculator

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.pow

object MathEvaluator {

    private val numberFormat = DecimalFormat("#,##0.######", DecimalFormatSymbols(Locale.US)).apply {
        isGroupingUsed = false
    }

    /**
     * Attempts to evaluate a string as a mathematical expression.
     * Returns null if the expression is invalid, incomplete, or not a math expression.
     */
    fun evaluate(query: String): String? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null

        // Quick heuristic check: must contain at least one operator and numbers
        val normalized = trimmed
            .replace('×', '*')
            .replace('x', '*')
            .replace('X', '*')
            .replace('÷', '/')
            .replace(" ", "")

        if (!isLikelyMathExpression(normalized)) {
            return null
        }

        return try {
            val result = Parser(normalized).parse()
            if (result.isNaN()) {
                null
            } else if (result.isInfinite()) {
                "Cannot divide by 0"
            } else {
                formatResult(result)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun isLikelyMathExpression(text: String): Boolean {
        if (text.isEmpty()) return false
        // Must contain at least one digit
        if (!text.any { it.isDigit() }) return false
        // Must contain at least one math operator: +, -, *, /, %, ^
        val hasOperator = text.any { it == '+' || it == '-' || it == '*' || it == '/' || it == '%' || it == '^' }
        if (!hasOperator) return false
        // Must only contain allowed characters: digits, ., +, -, *, /, %, ^, (, )
        return text.all { it.isDigit() || it == '.' || it == '+' || it == '-' || it == '*' || it == '/' || it == '%' || it == '^' || it == '(' || it == ')' }
    }

    private fun formatResult(value: Double): String {
        return if (value % 1.0 == 0.0 && value >= Long.MIN_VALUE.toDouble() && value <= Long.MAX_VALUE.toDouble()) {
            value.toLong().toString()
        } else {
            numberFormat.format(value)
        }
    }

    private class Parser(private val src: String) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            pos++
            ch = if (pos < src.length) src[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < src.length) throw IllegalArgumentException("Unexpected character: " + ch.toChar())
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) return Double.POSITIVE_INFINITY
                        x /= divisor
                    }
                    eat('%'.code) -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) return Double.POSITIVE_INFINITY
                        x %= divisor
                    }
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return +parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                if (!eat(')'.code)) throw IllegalArgumentException("Missing closing parenthesis")
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                val numStr = src.substring(startPos, pos)
                x = numStr.toDouble()
            } else {
                throw IllegalArgumentException("Unexpected character: " + ch.toChar())
            }

            if (eat('^'.code)) {
                val exponent = parseFactor()
                x = x.pow(exponent)
            }

            return x
        }
    }
}
