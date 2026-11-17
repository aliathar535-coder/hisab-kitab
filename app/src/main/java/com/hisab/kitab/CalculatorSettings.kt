
package com.hisab.kitab

import android.content.Context
import android.graphics.Color

class CalculatorSettings(context: Context) {

    private val prefs = context.getSharedPreferences(
        "calculator_custom_settings",
        Context.MODE_PRIVATE
    )

    fun getButtonColor(): Int =
        prefs.getInt("button_color", Color.rgb(37, 99, 235))

    fun setButtonColor(color: Int) {
        prefs.edit().putInt("button_color", color).apply()
    }

    fun getBackgroundColor(): Int =
        prefs.getInt("background_color", Color.rgb(15, 23, 42))

    fun setBackgroundColor(color: Int) {
        prefs.edit().putInt("background_color", color).apply()
    }

    fun getTextColor(): Int =
        prefs.getInt("text_color", Color.WHITE)

    fun setTextColor(color: Int) {
        prefs.edit().putInt("text_color", color).apply()
    }

    fun getButtonSize(): Int =
        prefs.getInt("button_size", 58).coerceIn(42, 82)

    fun setButtonSize(size: Int) {
        prefs.edit()
            .putInt("button_size", size.coerceIn(42, 82))
            .apply()
    }

    fun getCornerRadius(): Float =
        prefs.getFloat("corner_radius", 18f).coerceIn(4f, 32f)

    fun setCornerRadius(radius: Float) {
        prefs.edit()
            .putFloat("corner_radius", radius.coerceIn(4f, 32f))
            .apply()
    }

    fun reset() {
        prefs.edit().clear().apply()
    }
}
