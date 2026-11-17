
package com.hisab.kitab

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

class DesignStudio(private val context: Context) {

    private val manager = DesignManager(context)

    private val styles = listOf(
        "Royal 3D", "Neon Glow", "Fire", "Chrome", "Ice",
        "Nature", "Rainbow", "Wood", "Paper", "Glitch",
        "Speed", "Bubble", "Smoke", "Diamond", "Electric",
        "Glass", "Metallic", "Gold", "Silver", "Retro",
        "Cyber", "Shadow", "Outline", "Elegant", "Luxury",
        "Bold", "Minimal", "Gradient", "Classic", "Custom"
    )

    private val colors = listOf(
        0xFFFFD700.toInt(), 0xFF00E5FF.toInt(),
        0xFFFF5722.toInt(), 0xFFC0C0C0.toInt(),
        0xFF80DEEA.toInt(), 0xFF4CAF50.toInt(),
        0xFFE040FB.toInt(), 0xFF795548.toInt(),
        0xFFFFE0B2.toInt(), 0xFF00BCD4.toInt(),
        0xFF448AFF.toInt(), 0xFFFF80AB.toInt(),
        0xFFB0BEC5.toInt(), 0xFFB388FF.toInt(),
        0xFF00E676.toInt(), 0xFF80CBC4.toInt(),
        0xFFBDBDBD.toInt(), 0xFFFFD54F.toInt(),
        0xFFE0E0E0.toInt(), 0xFFFFAB91.toInt(),
        0xFF18FFFF.toInt(), 0xFFCE93D8.toInt(),
        0xFF80D8FF.toInt(), 0xFFF8BBD0.toInt(),
        0xFFFFE082.toInt(), 0xFFFFFFFF.toInt(),
        0xFFCFD8DC.toInt(), 0xFFB39DDB.toInt(),
        0xFFFFFFFF.toInt(), 0xFFF0ABFC.toInt()
    )

    fun show(): LinearLayout {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFF101827.toInt())
        }

        val title = TextView(context).apply {
            text = "TEXT STYLE GALLERY"
            textSize = 23f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        }
        root.addView(title)

        val instruction = TextView(context).apply {
            text = "हर नंबर के सामने असली डिजाइन का नमूना देखें"
            textSize = 13f
            setTextColor(0xFFB8C5D9.toInt())
            setPadding(0, 8, 0, 16)
        }
        root.addView(instruction)

        val grid = GridLayout(context).apply {
            columnCount = 2
        }

        styles.forEachIndexed { index, name ->
            val number = index + 1
            val styleColor = colors[index]

            val card = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(12, 16, 12, 16)

                background = GradientDrawable().apply {
                    setColor(0xFF202D42.toInt())
                    cornerRadius = 22f
                    setStroke(2, styleColor)
                }
            }

            val sample = TextView(context).apply {
                text = "मेरा हिसाब"
                textSize = 21f
                gravity = Gravity.CENTER
                setTextColor(styleColor)
                setTypeface(
                    Typeface.DEFAULT,
                    if (number % 3 == 0) Typeface.ITALIC
                    else Typeface.BOLD
                )
            }

            val label = TextView(context).apply {
                text = "No. $number  •  $name"
                textSize = 12f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(0, 10, 0, 0)
            }

            card.addView(sample)
            card.addView(label)

            card.setOnClickListener {
                manager.saveTextStyle(number)
                showPreview(name, number, styleColor)
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                columnSpec = GridLayout.spec(
                    GridLayout.UNDEFINED,
                    1f
                )
                setMargins(5, 5, 5, 5)
            }

            grid.addView(card, params)
        }

        root.addView(grid)
        return root
    }

    private fun showPreview(
        name: String,
        number: Int,
        color: Int
    ) {
        val preview = TextView(context).apply {
            text = "मेरा हिसाब"
            textSize = 30f
            setTextColor(color)
            gravity = Gravity.CENTER
            setPadding(20, 30, 20, 30)
        }

        android.app.AlertDialog.Builder(context)
            .setTitle("Design $number - $name")
            .setView(preview)
            .setNegativeButton("वापस", null)
            .setPositiveButton("Apply") { _, _ ->
                manager.saveTextStyle(number)
                manager.saveTextColor(color)
            }
            .show()
    }
}
