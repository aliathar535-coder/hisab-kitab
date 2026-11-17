package com.hisab.kitab

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
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
        return TextView(context).apply {
            text = value
            textSize = 26f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(4, 12, 4, 12)
        }
    }

    fun subtitle(context: Context, value: String): TextView {
        return TextView(context).apply {
            text = value
            textSize = 14f
            setTextColor(0xFFB8C5D9.toInt())
            setPadding(4, 6, 4, 6)
        }
    }

    fun card(
        context: Context,
        heading: String,
        value: String,
        accent: Int = 0xFF38BDF8.toInt(),
        boxId: String = "home_card"
    ): LinearLayout {
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20, 18, 20, 18)

            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    0xFF26364F.toInt(),
                    0xFF172438.toInt()
                )
            ).apply {
                cornerRadius = 24f
                setStroke(2, accent)
            }

            addView(TextView(context).apply {
                text = heading
                textSize = 14f
                setTextColor(0xFFB8C5D9.toInt())
            })

            addView(TextView(context).apply {
                text = value
                textSize = 25f
                setTextColor(Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 8, 0, 0)
            })
        }

        DesignManager(context).applyTo(card, boxId)
        return card
    }

    fun applyBox(context: Context, view: View, boxId: String) {
        DesignManager(context).applyTo(view, boxId)
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
            direction,
            intArrayOf(startColor, endColor)
        ).apply {
            cornerRadius = 22f
        }
    }
}
