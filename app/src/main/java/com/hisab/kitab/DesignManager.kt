
package com.hisab.kitab

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View

data class BoxDesign(
    val colors: IntArray,
    val gradientStyle: Int,
    val shadingStyle: Int,
    val angle: Int,
    val opacity: Int,
    val borderColor: Int,
    val borderWidth: Int,
    val radius: Int,
    val shadow: Float,
    val liquidGlass: Boolean
)

class DesignManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "hisab_design_settings", Context.MODE_PRIVATE
    )

    companion object {
        val gradientNames = listOf(
            "Royal Blue", "Ocean", "Sunset", "Fire", "Gold",
            "Purple Dream", "Emerald", "Aqua", "Rose", "Lavender",
            "Midnight", "Ice", "Neon", "Rainbow", "Peach",
            "Forest", "Cyber", "Candy", "Silver", "Bronze",
            "Sky", "Lime", "Ruby", "Galaxy", "Aurora",
            "Coffee", "Coral", "Arctic", "Luxury", "Liquid Glass"
        )

        val shadingNames = listOf(
            "Soft Shadow", "Deep Shadow", "3D Raised", "Inset Look",
            "Neon Edge", "Double Border", "Glass Shine", "Frosted",
            "Metal Edge", "Gold Edge", "Silver Edge", "Glow Look",
            "Soft Card", "Floating", "Dark Depth", "Light Depth",
            "Royal Frame", "Cyber Frame", "Rounded Glow", "Minimal",
            "Crystal", "Chrome", "Velvet", "Pearl", "Diamond",
            "Electric", "Outline", "Luxury", "Soft Frost", "Liquid Glass"
        )

        private val palettes = arrayOf(
            intArrayOf(0xFF102A56.toInt(), 0xFF2563EB.toInt(), 0xFF60A5FA.toInt()),
            intArrayOf(0xFF064E5B.toInt(), 0xFF0891B2.toInt(), 0xFF67E8F9.toInt()),
            intArrayOf(0xFF7C2D12.toInt(), 0xFFEA580C.toInt(), 0xFFFBBF24.toInt()),
            intArrayOf(0xFF7F1D1D.toInt(), 0xFFDC2626.toInt(), 0xFFF97316.toInt()),
            intArrayOf(0xFF78350F.toInt(), 0xFFD97706.toInt(), 0xFFFDE68A.toInt()),
            intArrayOf(0xFF3B0764.toInt(), 0xFF9333EA.toInt(), 0xFFE879F9.toInt()),
            intArrayOf(0xFF064E3B.toInt(), 0xFF059669.toInt(), 0xFF6EE7B7.toInt()),
            intArrayOf(0xFF164E63.toInt(), 0xFF06B6D4.toInt(), 0xFFA5F3FC.toInt()),
            intArrayOf(0xFF881337.toInt(), 0xFFE11D48.toInt(), 0xFFFDA4AF.toInt()),
            intArrayOf(0xFF4C1D95.toInt(), 0xFF8B5CF6.toInt(), 0xFFDDD6FE.toInt()),
            intArrayOf(0xFF020617.toInt(), 0xFF1E293B.toInt(), 0xFF475569.toInt()),
            intArrayOf(0xFF155E75.toInt(), 0xFFBAE6FD.toInt(), 0xFFFFFFFF.toInt()),
            intArrayOf(0xFF312E81.toInt(), 0xFF7C3AED.toInt(), 0xFF22D3EE.toInt()),
            intArrayOf(0xFFBE123C.toInt(), 0xFF7C3AED.toInt(), 0xFF2563EB.toInt()),
            intArrayOf(0xFF9A3412.toInt(), 0xFFFCA5A5.toInt(), 0xFFFDE68A.toInt()),
            intArrayOf(0xFF14532D.toInt(), 0xFF166534.toInt(), 0xFFBEF264.toInt()),
            intArrayOf(0xFF111827.toInt(), 0xFF4338CA.toInt(), 0xFF06B6D4.toInt()),
            intArrayOf(0xFF9D174D.toInt(), 0xFFEC4899.toInt(), 0xFFFBCFE8.toInt()),
            intArrayOf(0xFF334155.toInt(), 0xFF94A3B8.toInt(), 0xFFE2E8F0.toInt()),
            intArrayOf(0xFF451A03.toInt(), 0xFF92400E.toInt(), 0xFFD6A56D.toInt()),
            intArrayOf(0xFF1D4ED8.toInt(), 0xFF38BDF8.toInt(), 0xFFBAE6FD.toInt()),
            intArrayOf(0xFF365314.toInt(), 0xFF65A30D.toInt(), 0xFFD9F99D.toInt()),
            intArrayOf(0xFF7F1D1D.toInt(), 0xFFB91C1C.toInt(), 0xFFFCA5A5.toInt()),
            intArrayOf(0xFF1E1B4B.toInt(), 0xFF6D28D9.toInt(), 0xFFDB2777.toInt()),
            intArrayOf(0xFF134E4A.toInt(), 0xFF0D9488.toInt(), 0xFFA7F3D0.toInt()),
            intArrayOf(0xFF292524.toInt(), 0xFF78716C.toInt(), 0xFFD6D3D1.toInt()),
            intArrayOf(0xFF7C2D12.toInt(), 0xFFF97316.toInt(), 0xFFFECACA.toInt()),
            intArrayOf(0xFF0C4A6E.toInt(), 0xFF38BDF8.toInt(), 0xFFE0F2FE.toInt()),
            intArrayOf(0xFF422006.toInt(), 0xFFB45309.toInt(), 0xFFFFD700.toInt()),
            intArrayOf(0xAAFFFFFF.toInt(), 0x667EA6C9, 0x553B82F6)
        )
    }

    fun saveBoxDesign(boxId: String, design: BoxDesign) {
        val e = prefs.edit()
            .putInt("${boxId}_gradient", design.gradientStyle)
            .putInt("${boxId}_shade", design.shadingStyle)
            .putInt("${boxId}_angle", design.angle)
            .putInt("${boxId}_opacity", design.opacity)
            .putInt("${boxId}_border", design.borderColor)
            .putInt("${boxId}_border_width", design.borderWidth)
            .putInt("${boxId}_radius", design.radius)
            .putFloat("${boxId}_shadow", design.shadow)
            .putBoolean("${boxId}_glass", design.liquidGlass)
        for (i in 0..4) {
            e.putInt("${boxId}_color_$i", design.colors.getOrElse(i) {
                design.colors.lastOrNull() ?: Color.BLUE
            })
        }
        e.apply()
    }

    fun getBoxDesign(boxId: String): BoxDesign {
        val defaults = palettes[0]
        val colors = IntArray(5) { i ->
            prefs.getInt("${boxId}_color_$i", defaults[i % defaults.size])
        }
        return BoxDesign(
            colors,
            prefs.getInt("${boxId}_gradient", 0),
            prefs.getInt("${boxId}_shade", 0),
            prefs.getInt("${boxId}_angle", 45),
            prefs.getInt("${boxId}_opacity", 100),
            prefs.getInt("${boxId}_border", Color.WHITE),
            prefs.getInt("${boxId}_border_width", 2),
            prefs.getInt("${boxId}_radius", 24),
            prefs.getFloat("${boxId}_shadow", 8f),
            prefs.getBoolean("${boxId}_glass", false)
        )
    }

    fun saveForAll(design: BoxDesign) {
        prefs.edit().putBoolean("apply_design_to_all", true).apply()
        saveBoxDesign("global", design)
    }

    fun isApplyToAll(): Boolean =
        prefs.getBoolean("apply_design_to_all", false)

    fun setApplyToAll(enabled: Boolean) {
        prefs.edit().putBoolean("apply_design_to_all", enabled).apply()
    }

    fun drawable(boxId: String): GradientDrawable {
        val design = if (isApplyToAll()) getBoxDesign("global")
                     else getBoxDesign(boxId)
        val colors = design.colors.map { color ->
            val alpha = (Color.alpha(color) * design.opacity / 100)
                .coerceIn(0, 255)
            Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        }.toIntArray()

        val orientation = when (((design.angle % 360) + 360) % 360) {
            in 0..22, in 338..359 -> GradientDrawable.Orientation.LEFT_RIGHT
            in 23..67 -> GradientDrawable.Orientation.BL_TR
            in 68..112 -> GradientDrawable.Orientation.BOTTOM_TOP
            in 113..157 -> GradientDrawable.Orientation.BR_TL
            in 158..202 -> GradientDrawable.Orientation.RIGHT_LEFT
            in 203..247 -> GradientDrawable.Orientation.TR_BL
            in 248..292 -> GradientDrawable.Orientation.TOP_BOTTOM
            else -> GradientDrawable.Orientation.TL_BR
        }

        val glass = design.liquidGlass || design.gradientStyle == 29 ||
            design.shadingStyle == 29

        val drawable = GradientDrawable(orientation, colors).apply {
            cornerRadius = design.radius.toFloat()
            val border = if (glass) 0xCCFFFFFF.toInt() else design.borderColor
            val width = if (glass) maxOf(2, design.borderWidth) else design.borderWidth
            setStroke(width, border)
        }

        return drawable
    }

    fun applyTo(view: View, boxId: String) {
        val design = if (isApplyToAll()) getBoxDesign("global")
                     else getBoxDesign(boxId)
        view.background = drawable(boxId)
        view.elevation = design.shadow
        if (design.liquidGlass || design.gradientStyle == 29 ||
            design.shadingStyle == 29) {
            view.alpha = 0.98f
        } else {
            view.alpha = 1f
        }
    }

    fun gradientColors(index: Int): IntArray =
        palettes[index.coerceIn(0, palettes.lastIndex)].copyOf()

    fun saveTextColor(color: Int) {
        prefs.edit().putInt("text_color", color).apply()
    }

    fun getTextColor(): Int = prefs.getInt("text_color", Color.WHITE)

    fun saveTextSize(size: Float) {
        prefs.edit().putFloat("text_size", size).apply()
    }

    fun getTextSize(): Float = prefs.getFloat("text_size", 18f)

    fun saveTextWeight(weight: Int) {
        prefs.edit().putInt("text_weight", weight).apply()
    }

    fun getTextWeight(): Int = prefs.getInt("text_weight", 700)

    fun saveTextStyle(styleNumber: Int) {
        prefs.edit().putInt("text_style", styleNumber).apply()
    }

    fun getTextStyle(): Int = prefs.getInt("text_style", 1)

    fun saveGradient(startColor: Int, endColor: Int, angle: Int) {
        prefs.edit().putInt("gradient_start", startColor)
            .putInt("gradient_end", endColor)
            .putInt("gradient_angle", angle.coerceIn(0, 360)).apply()
    }

    fun getGradientStart(): Int =
        prefs.getInt("gradient_start", Color.rgb(30, 64, 175))

    fun getGradientEnd(): Int =
        prefs.getInt("gradient_end", Color.rgb(147, 51, 234))

    fun getGradientAngle(): Int = prefs.getInt("gradient_angle", 45)

    fun saveClockStyle(style: Int, color: Int, size: Float) {
        prefs.edit().putInt("clock_style", style).putInt("clock_color", color)
            .putFloat("clock_size", size).apply()
    }

    fun saveDateStyle(style: Int, color: Int, size: Float) {
        prefs.edit().putInt("date_style", style).putInt("date_color", color)
            .putFloat("date_size", size).apply()
    }

    fun saveCustomFont(path: String) {
        prefs.edit().putString("custom_font_path", path).apply()
    }

    fun getCustomFont(): String? = prefs.getString("custom_font_path", null)

    fun resetAll() {
        prefs.edit().clear().apply()
    }
}
