
package com.hisab.kitab

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import java.util.Locale

class CalculatorScreen(private val context: Context) {

    private val history = CalculatorHistoryManager(context)
    private val settings = CalculatorSettings(context)

    private var expression = ""
    private lateinit var display: TextView
    private lateinit var resultView: TextView
    private lateinit var root: LinearLayout
    private lateinit var grid: GridLayout

    fun show(): LinearLayout {
        root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 16)
            background = rounded(
                settings.getBackgroundColor(),
                24f
            )
        }

        val heading = TextView(context).apply {
            text = "✦ SMART CALCULATOR"
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 16)
            tag = "calc_drag_handle"
        }
        root.addView(heading)

        display = TextView(context).apply {
            text = "0"
            textSize = 27f
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setTextColor(settings.getTextColor())
            setPadding(12, 18, 12, 10)
            maxLines = 2
        }
        root.addView(display)

        resultView = TextView(context).apply {
            text = "जवाब यहाँ दिखेगा"
            textSize = 20f
            gravity = Gravity.END
            setTextColor(0xFF4ADE80.toInt())
            setPadding(12, 4, 12, 18)
        }
        root.addView(resultView)

        grid = GridLayout(context).apply {
            columnCount = 4
            useDefaultMargins = true
        }

        val keys = listOf(
            "AC", "⌫", "(", ")",
            "7", "8", "9", "÷",
            "4", "5", "6", "×",
            "1", "2", "3", "-",
            "0", ".", "%", "+",
            "HISTORY", "=", "SAVE", "⚙"
        )

        keys.forEach { key ->
            val button = Button(context).apply {
                text = key
                isAllCaps = false
                textSize = if (key.length > 2) 11f else 19f
                setTextColor(Color.WHITE)
                minHeight = settings.getButtonSize()
                elevation = 9f
                background = rounded(
                    when (key) {
                        "=", "SAVE" -> 0xFF16A34A.toInt()
                        "AC", "⌫" -> 0xFFDC3545.toInt()
                        "HISTORY", "⚙" -> 0xFF7C3AED.toInt()
                        "+", "-", "×", "÷", "%" -> 0xFF0EA5E9.toInt()
                        else -> settings.getButtonColor()
                    },
                    settings.getCornerRadius()
                )

                setOnClickListener {
                    when (key) {
                        "AC" -> expression = ""
                        "⌫" -> expression = expression.dropLast(1)
                        "=" -> saveAndCalculate()
                        "HISTORY" -> showHistory()
                        "SAVE" -> saveCurrent()
                        "⚙" -> showSettings()
                        else -> {
                            expression += when (key) {
                                "×" -> "*"
                                "÷" -> "/"
                                else -> key
                            }
                        }
                    }
                    updateDisplay()
                }
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = settings.getButtonSize()
                columnSpec = GridLayout.spec(
                    GridLayout.UNDEFINED,
                    1f
                )
                setMargins(4, 5, 4, 5)
            }

            grid.addView(button, params)
        }

        root.addView(grid)

        return root
    }

    private fun updateDisplay() {
        display.text = expression.ifEmpty { "0" }

        val answer = calculate(expression)

        resultView.text = if (answer != null && expression.isNotBlank()) {
            "= $answer"
        } else {
            "जवाब यहाँ दिखेगा"
        }
    }

    private fun saveAndCalculate() {
        val answer = calculate(expression) ?: return
        history.save(expression, answer)
        expression = answer
    }

    private fun saveCurrent() {
        val answer = calculate(expression)
        if (answer == null) {
            message("पहले सही हिसाब लिखें।")
            return
        }

        val id = history.save(expression, answer)
        if (id > 0) message("हिसाब हिस्ट्री में सेव हो गया।")
    }

    private fun showHistory() {
        val items = history.getAll()

        if (items.isEmpty()) {
            message("अभी कोई हिस्ट्री सेव नहीं है।")
            return
        }

        val list = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 8, 16, 8)
        }

        items.forEach { item ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(10, 10, 10, 10)
                background = rounded(0xFF263449.toInt(), 14f)
            }

            val detail = TextView(context).apply {
                text = "${item.expression} = ${item.result}\n${item.dateText()}"
                textSize = 15f
                setTextColor(Color.WHITE)
                setPadding(8, 5, 8, 5)
            }

            val delete = Button(context).apply {
                text = "यह हिसाब हटाएँ"
                isAllCaps = false
                setOnClickListener {
                    AlertDialog.Builder(context)
                        .setTitle("हिस्ट्री हटाएँ?")
                        .setMessage("क्या यह हिसाब हटाना चाहते हैं?")
                        .setNegativeButton("नहीं", null)
                        .setPositiveButton("हटाएँ") { _, _ ->
                            history.delete(item.id)
                            showHistory()
                        }
                        .show()
                }
            }

            row.addView(detail)
            row.addView(delete)

            val params = LinearLayout.LayoutParams(
                -1,
                -2
            )
            params.bottomMargin = 10
            list.addView(row, params)
        }

        val scroll = android.widget.ScrollView(context).apply {
            addView(list)
        }

        AlertDialog.Builder(context)
            .setTitle("कैलकुलेटर हिस्ट्री")
            .setView(scroll)
            .setNeutralButton("पूरी हिस्ट्री साफ") { _, _ ->
                AlertDialog.Builder(context)
                    .setTitle("पूरी हिस्ट्री हटाएँ?")
                    .setMessage("यह हिस्ट्री वापस नहीं आएगी।")
                    .setNegativeButton("रहने दें", null)
                    .setPositiveButton("सब हटाएँ") { _, _ ->
                        history.clearAll()
                        message("हिस्ट्री साफ हो गई।")
                    }
                    .show()
            }
            .setPositiveButton("बंद", null)
            .show()
    }

    private fun showSettings() {
        val panel = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 12, 20, 12)
        }

        fun addLabel(value: String) {
            panel.addView(TextView(context).apply {
                text = value
                textSize = 15f
                setTextColor(Color.WHITE)
                setPadding(0, 12, 0, 4)
            })
        }

        addLabel("बटन का रंग")

        val colors = listOf(
            0xFF2563EB.toInt(),
            0xFF7C3AED.toInt(),
            0xFF059669.toInt(),
            0xFFDB2777.toInt(),
            0xFFEA580C.toInt(),
            0xFF334155.toInt()
        )

        val colorRow = LinearLayout(context)
        colors.forEach { color ->
            val b = Button(context).apply {
                text = "●"
                setTextColor(color)
                setOnClickListener {
                    settings.setButtonColor(color)
                    message("रंग सेव हो गया। कैलकुलेटर दोबारा खोलें।")
                }
            }
            colorRow.addView(b, LinearLayout.LayoutParams(0, 55, 1f))
        }
        panel.addView(colorRow)

        addLabel("बटन का आकार")

        val sizeBar = SeekBar(context).apply {
            max = 40
            progress = settings.getButtonSize() - 42
        }
        panel.addView(sizeBar)

        addLabel("बटन की गोलाई")

        val roundBar = SeekBar(context).apply {
            max = 28
            progress = settings.getCornerRadius().toInt() - 4
        }
        panel.addView(roundBar)

        AlertDialog.Builder(context)
            .setTitle("कैलकुलेटर डिजाइन सेटिंग")
            .setView(panel)
            .setNegativeButton("बंद", null)
            .setNeutralButton("डिफॉल्ट") { _, _ ->
                settings.reset()
                message("डिफॉल्ट डिजाइन सेट हो गया।")
            }
            .setPositiveButton("सेव") { _, _ ->
                settings.setButtonSize(sizeBar.progress + 42)
                settings.setCornerRadius(
                    (roundBar.progress + 4).toFloat()
                )
                message("सेटिंग सेव हो गई।")
            }
            .show()
    }

    private fun rounded(color: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius * context.resources.displayMetrics.density
            setColor(color)
            setStroke(1, 0x447FFFFFFF)
        }
    }

    private fun message(value: String) {
        AlertDialog.Builder(context)
            .setMessage(value)
            .setPositiveButton("ठीक है", null)
            .show()
    }

    private fun calculate(value: String): String? {
        return try {
            if (!value.matches(Regex("[0-9+*/(). %\\-]+"))) {
                return null
            }

            val clean = value.replace("%", "/100")
            val result = Parser(clean).parse()

            if (!result.isFinite()) return null

            if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                String.format(Locale.US, "%.8f", result)
                    .trimEnd('0')
                    .trimEnd('.')
            }
        } catch (_: Exception) {
            null
        }
    }

    private class Parser(private val input: String) {
        private var pos = 0

        fun parse(): Double {
            val result = expression()
            spaces()
            require(pos == input.length)
            return result
        }

        private fun expression(): Double {
            var value = term()
            while (true) {
                spaces()
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
                spaces()
                value = when {
                    take('*') -> value * factor()
                    take('/') -> value / factor()
                    else -> return value
                }
            }
        }

        private fun factor(): Double {
            spaces()

            if (take('+')) return factor()
            if (take('-')) return -factor()

            if (take('(')) {
                val value = expression()
                require(take(')'))
                return value
            }

            val start = pos
            var dots = 0

            while (pos < input.length &&
                (input[pos].isDigit() || input[pos] == '.')
            ) {
                if (input[pos] == '.') dots++
                require(dots <= 1)
                pos++
            }

            require(pos > start)
            return input.substring(start, pos).toDouble()
        }

        private fun spaces() {
            while (pos < input.length && input[pos].isWhitespace()) {
                pos++
            }
        }

        private fun take(c: Char): Boolean {
            if (pos < input.length && input[pos] == c) {
                pos++
                return true
            }
            return false
        }
    }
}
