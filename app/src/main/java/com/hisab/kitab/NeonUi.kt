
package com.hisab.kitab

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

object NeonUi {
    fun background(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(0xFF0D1422.toInt())
        }
    }

    fun title(context: Context, value: String): TextView {
        return styledText(context, value, 26f, true, Color.WHITE).apply {
            setPadding(4, 12, 4, 12)
        }
    }

    fun subtitle(context: Context, value: String): TextView {
        return styledText(context, value, 14f, false, 0xFFB8C5D9.toInt()).apply {
            setPadding(4, 6, 4, 6)
        }
    }

    fun card(
        context: Context,
        heading: String,
        value: String,
        accent: Int = 0xFF38BDF8.toInt()
    ): LinearLayout {
        val manager = DesignManager(context)
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20, 18, 20, 18)

            background = makeCardBackground(manager, accent)
            elevation = when (manager.getAppStyle()) {
                "NEUMORPHIC" -> 14f
                "HOLOGRAPHIC" -> 18f
                "METALLIC" -> 10f
                else -> 16f
            }

            addView(styledText(
                context, heading, 14f, false, 0xFFD4DEED.toInt()
            ))
            addView(styledText(
                context, value, 25f, true, Color.WHITE
            ).apply {
                setPadding(0, 8, 0, 0)
            })
        }
    }

    private fun makeCardBackground(
        manager: DesignManager,
        accent: Int
    ): GradientDrawable {
        val style = manager.getAppStyle()
        val color = manager.getAppAccent()

        val colors = when (style) {
            "NEUMORPHIC" -> intArrayOf(
                0xFF39465A.toInt(), 0xFF202A3B.toInt()
            )
            "HOLOGRAPHIC" -> intArrayOf(
                0xFF321B65.toInt(), color,
                0xFF00B8D4.toInt(), 0xFF442266.toInt()
            )
            "METALLIC" -> intArrayOf(
                0xFFE1E6ED.toInt(), 0xFF8793A2.toInt(),
                0xFFDDE3EA.toInt(), 0xFF657181.toInt()
            )
            else -> intArrayOf(
                0xCCFFFFFF.toInt(), color,
                0xFF243B55.toInt(), 0xFF101E32.toInt()
            )
        }

        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR, colors
        ).apply {
            cornerRadius = when (style) {
                "NEUMORPHIC" -> 30f
                "METALLIC" -> 13f
                else -> 24f
            }
            setStroke(
                if (style == "LIQUID") 3 else 2,
                when (style) {
                    "HOLOGRAPHIC" -> color
                    "METALLIC" -> 0xFFE8EDF4.toInt()
                    "NEUMORPHIC" -> 0xFF65758A.toInt()
                    else -> 0xCCFFFFFF.toInt()
                }
            )
        }
    }

    private fun styledText(
        context: Context,
        value: String,
        defaultSize: Float,
        bold: Boolean,
        defaultColor: Int
    ): TextView {
        val manager = DesignManager(context)
        return TextView(context).apply {
            text = value
            textSize = defaultSize
            setTextColor(defaultColor)
            setTypeface(null, if (bold) Typeface.BOLD else Typeface.NORMAL)
            manager.applyFont(this)
        }
    }

    fun gradient(
        startColor: Int,
        endColor: Int,
        angle: Int
    ): GradientDrawable {
        val direction = when (((angle % 360) + 360) % 360) {
            in 0..44 -> GradientDrawable.Orientation.LEFT_RIGHT
            in 45..134 -> GradientDrawable.Orientation.BL_TR
            in 135..224 -> GradientDrawable.Orientation.BOTTOM_TOP
            in 225..314 -> GradientDrawable.Orientation.BR_TL
            else -> GradientDrawable.Orientation.RIGHT_LEFT
        }
        return GradientDrawable(
            direction, intArrayOf(startColor, endColor)
        ).apply {
            cornerRadius = 22f
        }
    }
}
