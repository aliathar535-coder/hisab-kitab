package com.hisab.kitab

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class FontStudio(private val context: Context) {

    private val manager = DesignManager(context)

    private var size = manager.getTextSize()
    private var weight = manager.getTextWeight()
    private var textColor = manager.getTextColor()
    private var italic = false

    fun show(): LinearLayout {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(0xFF101827.toInt())
        }

        val heading = TextView(context).apply {
            text = "ADVANCED TEXT STUDIO"
            textSize = 23f
            setTextColor(Color.WHITE)
        }

        val preview = TextView(context).apply {
            text = "मेरा हिसाब 123"
            gravity = Gravity.CENTER
            setPadding(15, 25, 15, 25)
            setBackgroundColor(0xFF202D42.toInt())
        }

        fun refresh() {
            preview.textSize = size
            preview.setTextColor(textColor)
            preview.setTypeface(
                Typeface.DEFAULT,
                weight or if (italic) Typeface.ITALIC else Typeface.NORMAL
            )
        }

        root.addView(heading)
        root.addView(preview)

        addLabel(root, "Text Size: ${size.toInt()}")

        val sizeSlider = SeekBar(context).apply {
            max = 48
            progress = (size - 12).toInt().coerceIn(0, 48)
            setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        size = (progress + 12).toFloat()
                        refresh()
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                }
            )
        }
        root.addView(sizeSlider)

        addLabel(root, "Text Thickness")

        val weights = listOf(
            "Thin" to 200,
            "Normal" to 400,
            "Bold" to 700,
            "Extra Bold" to 900
        )

        weights.forEach { (label, value) ->
            addButton(root, label) {
                weight = value
                refresh()
            }
        }

        addLabel(root, "Text Colour")

        val palette = listOf(
            Color.WHITE, Color.CYAN, Color.YELLOW,
            Color.RED, Color.GREEN, Color.MAGENTA,
            0xFFFF9800.toInt(), 0xFFB39DDB.toInt()
        )

        palette.forEach { color ->
            addButton(root, "Colour") {
                textColor = color
                refresh()
            }.setTextColor(color)
        }

        addButton(root, "Italic On / Off") {
            italic = !italic
            refresh()
        }

        addButton(root, "Save Text Style") {
            manager.saveTextSize(size)
            manager.saveTextWeight(weight)
            manager.saveTextColor(textColor)
        }

        refresh()
        return root
    }

    private fun addLabel(root: LinearLayout, label: String) {
        root.addView(TextView(context).apply {
            text = label
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(0, 14, 0, 5)
        })
    }

    private fun addButton(
        root: LinearLayout,
        label: String,
        action: () -> Unit
    ): Button {
        val button = Button(context).apply {
            text = label
            isAllCaps = false
            setOnClickListener { action() }
        }
        root.addView(button)
        return button
    }
}
