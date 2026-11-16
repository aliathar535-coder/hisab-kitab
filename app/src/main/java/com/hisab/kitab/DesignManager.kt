package com.hisab.kitab

import android.content.Context
import android.graphics.Color

class DesignManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "hisab_design_settings",
        Context.MODE_PRIVATE
    )

    fun saveBoxColor(boxId: String, color: Int) {
        prefs.edit().putInt("box_$boxId", color).apply()
    }

    fun getBoxColor(boxId: String): Int {
        return prefs.getInt("box_$boxId", Color.rgb(32, 45, 66))
    }

    fun saveTextColor(color: Int) {
        prefs.edit().putInt("text_color", color).apply()
    }

    fun getTextColor(): Int {
        return prefs.getInt("text_color", Color.WHITE)
    }

    fun saveTextSize(size: Float) {
        prefs.edit().putFloat("text_size", size).apply()
    }

    fun getTextSize(): Float {
        return prefs.getFloat("text_size", 18f)
    }

    fun saveTextWeight(weight: Int) {
        prefs.edit().putInt("text_weight", weight).apply()
    }

    fun getTextWeight(): Int {
        return prefs.getInt("text_weight", 700)
    }

    fun saveTextStyle(styleNumber: Int) {
        prefs.edit().putInt("text_style", styleNumber).apply()
    }

    fun getTextStyle(): Int {
        return prefs.getInt("text_style", 1)
    }

    fun saveGradient(
        startColor: Int,
        endColor: Int,
        angle: Int
    ) {
        prefs.edit()
            .putInt("gradient_start", startColor)
            .putInt("gradient_end", endColor)
            .putInt("gradient_angle", angle.coerceIn(0, 360))
            .apply()
    }

    fun getGradientStart(): Int =
        prefs.getInt("gradient_start", Color.rgb(30, 64, 175))

    fun getGradientEnd(): Int =
        prefs.getInt("gradient_end", Color.rgb(147, 51, 234))

    fun getGradientAngle(): Int =
        prefs.getInt("gradient_angle", 45)

    fun saveClockStyle(style: Int, color: Int, size: Float) {
        prefs.edit()
            .putInt("clock_style", style)
            .putInt("clock_color", color)
            .putFloat("clock_size", size)
            .apply()
    }

    fun saveDateStyle(style: Int, color: Int, size: Float) {
        prefs.edit()
            .putInt("date_style", style)
            .putInt("date_color", color)
            .putFloat("date_size", size)
            .apply()
    }

    fun saveCustomFont(path: String) {
        prefs.edit().putString("custom_font_path", path).apply()
    }

    fun getCustomFont(): String? =
        prefs.getString("custom_font_path", null)

    fun resetAll() {
        prefs.edit().clear().apply()
    }
}
