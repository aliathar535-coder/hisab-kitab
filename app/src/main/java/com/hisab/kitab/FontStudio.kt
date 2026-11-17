
package com.hisab.kitab

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.widget.*

class FontStudio(private val context: Context) {
    private val manager = DesignManager(context)
    private var size = manager.getTextSize()
    private var weight = manager.getTextWeight()
    private var color = manager.getTextColor()
    private var family = manager.getFontFamily()
    private var italic = manager.getFontItalic()
    private var shadow = manager.getFontShadow()
    private var outline = manager.getFontOutline()
    private var spacing = manager.getFontSpacing()
    private var previewText = "मेरा हिसाब 123"

    private lateinit var preview: TextView
    private lateinit var sizeLabel: TextView

    private val families = listOf(
        "Default" to "sans-serif",
        "Modern" to "sans-serif-medium",
        "Rounded" to "sans-serif-rounded",
        "Condensed" to "sans-serif-condensed",
        "Serif" to "serif",
        "Monospace" to "monospace",
        "Casual" to "casual",
        "Cursive" to "cursive",
        "Elegant" to "serif"
    )

    fun show(): LinearLayout {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 24)
            setBackgroundColor(0xFF101827.toInt())
        }

        root.addView(label("FONT STUDIO • ADVANCED"))
        root.addView(label("अपना टेक्स्ट लिखें और असली Font Design देखें"))

        val edit = EditText(context).apply {
            setText(previewText)
            setTextColor(Color.WHITE)
            textSize = 18f
            hint = "अपना टेक्स्ट लिखें"
            setHintTextColor(0xFF9CA3AF.toInt())
            setSingleLine(false)
            setPadding(14, 12, 14, 12)
            background = panel()
        }
        root.addView(edit)

        preview = TextView(context).apply {
            text = previewText
            gravity = Gravity.CENTER
            textSize = size
            setPadding(18, 30, 18, 30)
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(0xFF26364F.toInt(), 0xFF111827.toInt())
            ).apply {
                cornerRadius = 26f
                setStroke(2, 0xFF38BDF8.toInt())
            }
        }
        root.addView(preview, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = 14
            bottomMargin = 14
        })

        edit.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                previewText = s?.toString() ?: ""
                refresh()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        sizeLabel = label("FONT SIZE: ${size.toInt()} SP")
        root.addView(sizeLabel)
        root.addView(SeekBar(context).apply {
            max = 48
            progress = (size - 12).toInt().coerceIn(0, 48)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, user: Boolean) {
                    size = (p + 12).toFloat()
                    sizeLabel.text = "FONT SIZE: ${size.toInt()} SP"
                    refresh()
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        })

        root.addView(label("FONT FAMILY"))
        families.forEach { (name, value) ->
            addButton(root, "Aa  $name") {
                family = value
                refresh()
            }
        }

        root.addView(label("FONT WEIGHT"))
        listOf(
            "Normal" to 400,
            "Medium" to 500,
            "Bold" to 700,
            "Extra Bold" to 900
        ).forEach { (name, value) ->
            addButton(root, name) {
                weight = value
                refresh()
            }
        }

        root.addView(label("FONT EFFECTS"))
        addButton(root, "Italic ON / OFF") {
            italic = !italic
            refresh()
        }
        addButton(root, "Shadow ON / OFF") {
            shadow = !shadow
            refresh()
        }
        addButton(root, "Outline ON / OFF") {
            outline = !outline
            refresh()
        }

        root.addView(label("LETTER SPACING"))
        root.addView(SeekBar(context).apply {
            max = 30
            progress = ((spacing + 2f) * 10).toInt().coerceIn(0, 30)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, user: Boolean) {
                    spacing = p / 10f - 2f
                    refresh()
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        })

        root.addView(label("FONT COLOR"))
        val colors = listOf(
            Color.WHITE, Color.YELLOW, Color.CYAN, Color.RED,
            Color.GREEN, Color.MAGENTA, 0xFFFF9800.toInt(),
            0xFFB39DDB.toInt(), 0xFF80CBC4.toInt(),
            0xFFFF80AB.toInt(), 0xFF90CAF9.toInt(),
            0xFFFFD700.toInt(), 0xFFC0C0C0.toInt()
        )
        colors.forEach { c ->
            addButton(root, "● रंग चुनें") {
                color = c
                refresh()
            }.setTextColor(c)
        }

        addButton(root, "अपना HEX रंग डालें") {
            val input = EditText(context).apply {
                hint = "#FF00CC"
                setSingleLine(true)
                inputType = InputType.TYPE_CLASS_TEXT
            }
            AlertDialog.Builder(context)
                .setTitle("Custom Font Color")
                .setView(input)
                .setPositiveButton("Apply") { _, _ ->
                    try {
                        color = Color.parseColor(input.text.toString().trim())
                        refresh()
                    } catch (_: Exception) {
                        Toast.makeText(context, "गलत HEX रंग", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        addButton(root, "SAVE FONT STYLE") {
            manager.saveTextSize(size)
            manager.saveTextWeight(weight)
            manager.saveTextColor(color)
            manager.saveFontFamily(family)
            manager.saveFontItalic(italic)
            manager.saveFontShadow(shadow)
            manager.saveFontOutline(outline)
            manager.saveFontSpacing(spacing)
            Toast.makeText(context, "Font Style सेव हो गया", Toast.LENGTH_SHORT).show()
        }

        refresh()
        return root
    }

    private fun refresh() {
        if (!::preview.isInitialized) return
        preview.text = previewText
        preview.textSize = size
        preview.setTextColor(color)
        preview.letterSpacing = spacing
        val style = if (italic) Typeface.ITALIC else Typeface.NORMAL
        preview.typeface = Typeface.create(family, style)
        preview.paint.isFakeBoldText = weight >= 700
        preview.paint.style = if (outline) Paint.Style.STROKE else Paint.Style.FILL
        preview.paint.strokeWidth = if (outline) 2.2f else 0f
        if (shadow) {
            preview.setShadowLayer(7f, 2f, 3f, color)
        } else {
            preview.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
        }
        preview.invalidate()
    }

    private fun panel() = GradientDrawable().apply {
        setColor(0xFF202D42.toInt())
        cornerRadius = 18f
        setStroke(1, 0xFF475569.toInt())
    }

    private fun label(value: String) = TextView(context).apply {
        text = value
        textSize = 14f
        setTextColor(Color.WHITE)
        setPadding(0, 14, 0, 8)
    }

    private fun addButton(
        root: LinearLayout,
        value: String,
        action: () -> Unit
    ): Button {
        val button = Button(context).apply {
            text = value
            isAllCaps = false
            setOnClickListener { action() }
        }
        root.addView(button)
        return button
    }
}
