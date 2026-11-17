
package com.hisab.kitab

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.widget.*

class DesignStudio(private val context: Context) {
    private val manager = DesignManager(context)
    private var accent = manager.getAppAccent()

    private val styles = listOf(
        "LIQUID" to "Liquid Glass 3D",
        "NEUMORPHIC" to "Neumorphic 3D",
        "HOLOGRAPHIC" to "Holographic 3D",
        "METALLIC" to "Metallic 3D"
    )

    fun show(): LinearLayout {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 18, 16, 24)
            setBackgroundColor(0xFF101827.toInt())
        }

        root.addView(label("APP DESIGN STUDIO"))
        root.addView(label("चार अलग-अलग असली डिजाइन स्टाइल"))

        styles.forEach { (key, name) ->
            val card = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(18, 20, 18, 20)
                background = styleDrawable(key)
                elevation = 12f
            }

            card.addView(TextView(context).apply {
                text = "हिसाब किताब  •  ₹ 12345"
                textSize = 21f
                gravity = Gravity.CENTER
                setTextColor(if (key == "METALLIC") Color.BLACK else Color.WHITE)
                setTypeface(null, Typeface.BOLD)
            })
            card.addView(label(name))

            root.addView(card, LinearLayout.LayoutParams(-1, 112).apply {
                bottomMargin = 14
            })

            card.setOnClickListener {
                manager.saveAppStyle(key, accent)
                Toast.makeText(context, "$name चुना गया", Toast.LENGTH_SHORT).show()
            }
        }

        root.addView(label("अपनी पसंद का कोई भी रंग चुनें"))
        root.addView(Button(context).apply {
            text = "CUSTOM COLOR / HEX"
            isAllCaps = false
            setOnClickListener { chooseColor() }
        })

        root.addView(Button(context).apply {
            text = "SAVE DESIGN"
            isAllCaps = false
            setOnClickListener {
                manager.saveAppStyle(manager.getAppStyle(), accent)
                Toast.makeText(context, "डिजाइन सेव हो गया", Toast.LENGTH_SHORT).show()
            }
        })

        root.addView(label("चुना हुआ स्टाइल: ${manager.getAppStyle()}"))
        return root
    }

    private fun chooseColor() {
        val input = EditText(context).apply {
            hint = "#38BDF8"
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
        }
        AlertDialog.Builder(context)
            .setTitle("अपना HEX रंग लिखें")
            .setView(input)
            .setPositiveButton("Apply") { _, _ ->
                try {
                    accent = Color.parseColor(input.text.toString().trim())
                    manager.saveAppStyle(manager.getAppStyle(), accent)
                    Toast.makeText(context, "रंग सेव हो गया", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                    Toast.makeText(context, "गलत HEX रंग", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun styleDrawable(style: String): GradientDrawable {
        val colors = when (style) {
            "NEUMORPHIC" -> intArrayOf(0xFF3A4659.toInt(), 0xFF202A3B.toInt())
            "HOLOGRAPHIC" -> intArrayOf(
                0xFF321B65.toInt(), accent, 0xFF00D4FF.toInt(), 0xFF6A1B9A.toInt()
            )
            "METALLIC" -> intArrayOf(
                0xFFE7ECF2.toInt(), 0xFF929EAC.toInt(),
                0xFFE8EDF4.toInt(), 0xFF596675.toInt()
            )
            else -> intArrayOf(
                0xAAFFFFFF.toInt(), accent, 0xAA172438.toInt()
            )
        }
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR, colors
        ).apply {
            cornerRadius = when (style) {
                "NEUMORPHIC" -> 34f
                "METALLIC" -> 12f
                else -> 26f
            }
            setStroke(
                3,
                when (style) {
                    "HOLOGRAPHIC" -> accent
                    "METALLIC" -> Color.WHITE
                    else -> 0xCCFFFFFF.toInt()
                }
            )
        }
    }

    private fun label(value: String) = TextView(context).apply {
        text = value
        textSize = 15f
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER_VERTICAL
        setPadding(4, 10, 4, 10)
    }
}
