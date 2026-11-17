package com.hisab.kitab

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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

    private val palette = listOf(
        Color.WHITE, Color.CYAN, Color.YELLOW, Color.RED,
        Color.GREEN, Color.MAGENTA, 0xFFFF9800.toInt(),
        0xFFB39DDB.toInt(), 0xFF80CBC4.toInt(),
        0xFFFF80AB.toInt(), 0xFF90CAF9.toInt()
    )

    fun show(): LinearLayout {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(0xFF101827.toInt())
        }

        val heading = TextView(context).apply {
            text = "ADVANCED TEXT STUDIO"
            textSize = 23f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        }
        root.addView(heading)

        val preview = TextView(context).apply {
            text = "मेरा हिसाब 123"
            gravity = Gravity.CENTER
            setPadding(15, 28, 15, 28)
            background = GradientDrawable().apply {
                setColor(0xFF202D42.toInt())
                cornerRadius = 24f
                setStroke(2, 0xFF38BDF8.toInt())
            }
        }
        root.addView(preview)

        fun refresh() {
            preview.textSize = size
            preview.setTextColor(textColor)
            val style = when {
                italic && weight >= 700 -> Typeface.BOLD_ITALIC
                italic -> Typeface.ITALIC
                weight >= 700 -> Typeface.BOLD
                else -> Typeface.NORMAL
            }
            preview.setTypeface(Typeface.DEFAULT, style)
        }

        addLabel(root, "TEXT SIZE: ${size.toInt()} SP")

        root.addView(SeekBar(context).apply {
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
        })

        addLabel(root, "TEXT THICKNESS")
        listOf(
            "Normal" to 400,
            "Bold" to 700,
            "Extra Bold" to 900
        ).forEach { (name, value) ->
            addButton(root, name) {
                weight = value
                refresh()
            }
        }

        addLabel(root, "TEXT COLOR")
        palette.forEach { color ->
            addButton(root, "● रंग चुनें") {
                textColor = color
                refresh()
            }.setTextColor(color)
        }

        addButton(root, "Italic On / Off") {
            italic = !italic
            refresh()
        }

        addButton(root, "SAVE TEXT STYLE") {
            manager.saveTextSize(size)
            manager.saveTextWeight(weight)
            manager.saveTextColor(textColor)
            android.app.AlertDialog.Builder(context)
                .setMessage("Text style सेव हो गया।")
                .setPositiveButton("ठीक है", null)
                .show()
        }

        refresh()
        return root
    }

    private fun addLabel(root: LinearLayout, value: String) {
        root.addView(TextView(context).apply {
            text = value
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(0, 14, 0, 6)
        })
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
