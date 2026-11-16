package com.hisab.kitab

import android.content.Context
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import javax.script.ScriptEngineManager

class CalculatorScreen(private val context: Context) {

    private var expression = ""

    fun show(): LinearLayout {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(0xFF101827.toInt())
        }

        val display = TextView(context).apply {
            text = "0"
            textSize = 30f
            gravity = Gravity.END
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(12, 25, 12, 25)
        }

        root.addView(display)

        val grid = GridLayout(context).apply {
            columnCount = 4
        }

        val keys = listOf(
            "7", "8", "9", "÷",
            "4", "5", "6", "×",
            "1", "2", "3", "-",
            "0", ".", "C", "+",
            "⌫", "=", "(", ")"
        )

        keys.forEach { key ->
            val button = Button(context).apply {
                text = key
                textSize = 18f
                setOnClickListener {
                    when (key) {
                        "C" -> expression = ""
                        "⌫" -> expression = expression.dropLast(1)
                        "=" -> {
                            val result = calculate(expression)
                            expression = result ?: expression
                        }
                        else -> expression += when (key) {
                            "×" -> "*"
                            "÷" -> "/"
                            else -> key
                        }
                    }
                    display.text = expression.ifEmpty { "0" }
                }
            }
            grid.addView(button)
        }

        root.addView(grid)
        return root
    }

    private fun calculate(value: String): String? {
        return try {
            if (!value.matches(Regex("[0-9+*/(). -]+"))) return null

            val result = Parser(value).parse()
            if (!result.isFinite()) return null

            if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                result.toString()
            }
        } catch (e: Exception) {
            null
        }
    }

    private class Parser(private val input: String) {
        private var pos = 0

        fun parse(): Double {
            val result = expression()
            skipSpaces()
            require(pos == input.length)
            return result
        }

        private fun expression(): Double {
            var value = term()
            while (true) {
                skipSpaces()
                value = when {
                    take('+') -> value + term()
                    take('-') -> value - term()
                    else -> return value
                }
            }
        }

        private fun term(): Double {
            var value = factor()
            while (true) {
                skipSpaces()
                value = when {
                    take('*') -> value * factor()
                    take('/') -> value / factor()
                    else -> return value
                }
            }
        }

        private fun factor(): Double {
            skipSpaces()
            if (take('+')) return factor()
            if (take('-')) return -factor()

            if (take('(')) {
                val value = expression()
                require(take(')'))
                return value
            }

            val start = pos
            while (pos < input.length &&
                (input[pos].isDigit() || input[pos] == '.')) {
                pos++
            }

            require(start < pos)
            return input.substring(start, pos).toDouble()
        }

        private fun skipSpaces() {
            while (pos < input.length && input[pos].isWhitespace()) pos++
        }

        private fun take(char: Char): Boolean {
            if (pos < input.length && input[pos] == char) {
                pos++
                return true
            }
            return false
        }
    }
}
