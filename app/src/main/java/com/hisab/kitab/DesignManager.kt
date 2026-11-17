
package com.hisab.kitab

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View

data class BoxDesign(
    val colors: IntArray,
    val gradientStyle: Int = 0,
    val shadingStyle: Int = 0,
    val angle: Int = 45,
    val opacity: Int = 100,
    val borderColor: Int = Color.WHITE,
    val borderWidth: Int = 1,
    val radius: Int = 22,
    val shadow: Float = 8f,
    val liquidGlass: Boolean = false
)

class DesignManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "hisab_design_settings",
        Context.MODE_PRIVATE
    )

    val gradientNames = listOf(
        "Royal Blue", "Ocean", "Sunset", "Fire", "Neon",
        "Emerald", "Purple Dream", "Gold", "Silver", "Rose",
        "Aqua", "Cyber", "Midnight", "Sky", "Forest",
        "Candy", "Lava", "Ice", "Rainbow", "Copper",
        "Lavender", "Lime", "Ruby", "Sapphire", "Chrome",
        "Peach", "Mint", "Galaxy", "Classic", "Custom"
    )

    val shadingNames = listOf(
        "Soft Shadow", "Deep Shadow", "3D Raised", "Inset",
        "Neon Glow", "Glass", "Frosted Look", "Metallic",
        "Glossy", "Matte", "Soft Light", "Dark Edge",
        "Double Border", "Royal", "Floating", "Sharp",
        "Smooth", "Crystal", "Diamond", "Chrome",
        "Gold Edge", "Silver Edge", "Cyber", "Glow",
        "Soft 3D", "Hard 3D", "Elegant", "Luxury",
        "Minimal", "Custom"
    )

    private val palettes = listOf(
        intArrayOf(0xFF123B68.toInt(), 0xFF176B87.toInt(), 0xFF1B9AAA.toInt(), 0xFF243B55.toInt(), 0xFF141E30.toInt()),
        intArrayOf(0xFF005C97.toInt(), 0xFF363795.toInt(), 0xFF00B4DB.toInt(), 0xFF0083B0.toInt(), 0xFF001F3F.toInt()),
        intArrayOf(0xFFFF512F.toInt(), 0xFFDD2476.toInt(), 0xFFFF9966.toInt(), 0xFFFF5E62.toInt(), 0xFF7F00FF.toInt()),
        intArrayOf(0xFF7F0000.toInt(), 0xFFFF512F.toInt(), 0xFFFFA500.toInt(), 0xFFB22222.toInt(), 0xFF3D0000.toInt()),
        intArrayOf(0xFF141E30.toInt(), 0xFF243B55.toInt(), 0xFF00C9FF.toInt(), 0xFF92FE9D.toInt(), 0xFF090979.toInt()),
        intArrayOf(0xFF134E5E.toInt(), 0xFF71B280.toInt(), 0xFF00A86B.toInt(), 0xFF2E8B57.toInt(), 0xFF003B2F.toInt()),
        intArrayOf(0xFF41295A.toInt(), 0xFF2F0743.toInt(), 0xFF8E2DE2.toInt(), 0xFFDA22FF.toInt(), 0xFF240046.toInt()),
        intArrayOf(0xFFF7971E.toInt(), 0xFFFFD200.toInt(), 0xFFB8860B.toInt(), 0xFFFFE259.toInt(), 0xFF8B6508.toInt()),
        intArrayOf(0xFF757F9A.toInt(), 0xFFD7DDE8.toInt(), 0xFFBFC7D5.toInt(), 0xFF8E9EAB.toInt(), 0xFF434A54.toInt()),
        intArrayOf(0xFFED4264.toInt(), 0xFFFFEDBC.toInt(), 0xFFFF9A9E.toInt(), 0xFFFAD0C4.toInt(), 0xFFB24592.toInt()),
        intArrayOf(0xFF00D2FF.toInt(), 0xFF3A7BD5.toInt(), 0xFF00F5D4.toInt(), 0xFF00A6FB.toInt(), 0xFF003566.toInt()),
        intArrayOf(0xFF0F2027.toInt(), 0xFF203A43.toInt(), 0xFF2C5364.toInt(), 0xFF00F5FF.toInt(), 0xFF111111.toInt()),
        intArrayOf(0xFF000428.toInt(), 0xFF004E92.toInt(), 0xFF141E30.toInt(), 0xFF243B55.toInt(), 0xFF000000.toInt()),
        intArrayOf(0xFF2980B9.toInt(), 0xFF6DD5FA.toInt(), 0xFFFFFFFF.toInt(), 0xFF2193B0.toInt(), 0xFF6DD5ED.toInt()),
        intArrayOf(0xFF1D4350.toInt(), 0xFFA43931.toInt(), 0xFF134E5E.toInt(), 0xFF71B280.toInt(), 0xFF003B2F.toInt()),
        intArrayOf(0xFFFF9A9E.toInt(), 0xFFFAD0C4.toInt(), 0xFFFBC2EB.toInt(), 0xFFA18CD1.toInt(), 0xFFFFC3A0.toInt()),
        intArrayOf(0xFF200122.toInt(), 0xFF6F0000.toInt(), 0xFFFF4B1F.toInt(), 0xFFFF9068.toInt(), 0xFF3D0000.toInt()),
        intArrayOf(0xFF83A4D4.toInt(), 0xFFB6FBFF.toInt(), 0xFFE0FFFF.toInt(), 0xFF89F7FE.toInt(), 0xFF4FACFE.toInt()),
        intArrayOf(0xFFFF0000.toInt(), 0xFFFF9900.toInt(), 0xFFFFFF00.toInt(), 0xFF00CC66.toInt(), 0xFF3366FF.toInt()),
        intArrayOf(0xFFB87333.toInt(), 0xFFDA8A67.toInt(), 0xFF8C4A2F.toInt(), 0xFFE6A57E.toInt(), 0xFF4A2511.toInt()),
        intArrayOf(0xFFB39DDB.toInt(), 0xFF9575CD.toInt(), 0xFFD1C4E9.toInt(), 0xFF7E57C2.toInt(), 0xFF311B92.toInt()),
        intArrayOf(0xFF76FF03.toInt(), 0xFF64DD17.toInt(), 0xFFB2FF59.toInt(), 0xFF00C853.toInt(), 0xFF1B5E20.toInt()),
        intArrayOf(0xFF8E0E00.toInt(), 0xFFB31217.toInt(), 0xFFFF416C.toInt(), 0xFFFF4B2B.toInt(), 0xFF4A0000.toInt()),
        intArrayOf(0xFF000046.toInt(), 0xFF1CB5E0.toInt(), 0xFF536976.toInt(), 0xFF292E49.toInt(), 0xFF000428.toInt()),
        intArrayOf(0xFF434343.toInt(), 0xFF000000.toInt(), 0xFFB8B8B8.toInt(), 0xFF666666.toInt(), 0xFF202020.toInt()),
        intArrayOf(0xFFFFD3A5.toInt(), 0xFFFD6585.toInt(), 0xFFFF9A9E.toInt(), 0xFFFECFEF.toInt(), 0xFFFFB199.toInt()),
        intArrayOf(0xFF11998E.toInt(), 0xFF38EF7D.toInt(), 0xFFA8E6CF.toInt(), 0xFF56AB2F.toInt(), 0xFF00695C.toInt()),
        intArrayOf(0xFF0F0C29.toInt(), 0xFF302B63.toInt(), 0xFF24243E.toInt(), 0xFF6A3093.toInt(), 0xFFA044FF.toInt()),
        intArrayOf(0xFF283048.toInt(), 0xFF859398.toInt(), 0xFF485563.toInt(), 0xFF29323C.toInt(), 0xFF1C1C1C.toInt()),
        intArrayOf(0xFF202D42.toInt(), 0xFF26364F.toInt(), 0xFF304A68.toInt(), 0xFF172438.toInt(), 0xFF101827.toInt())
    )

    fun gradientColors(index: Int): IntArray {
        return palettes[index.coerceIn(0, palettes.lastIndex)].copyOf()
    }

    fun isApplyToAll(): Boolean =
        prefs.getBoolean("apply_all", false)

    fun setApplyToAll(value: Boolean) {
        prefs.edit().putBoolean("apply_all", value).apply()
    }

    fun saveForAll(design: BoxDesign) {
        saveDesign("all_boxes", design)
        setApplyToAll(true)
    }

    fun saveBoxDesign(id: String, design: BoxDesign) {
        saveDesign("box_$id", design)
    }

    private fun saveDesign(key: String, design: BoxDesign) {
        val edit = prefs.edit()
        for (i in 0..4) {
            edit.putInt("${key}_color_$i", design.colors.getOrElse(i) {
                design.colors.lastOrNull() ?: Color.DKGRAY
            })
        }
        edit.putInt("${key}_gradient", design.gradientStyle)
        edit.putInt("${key}_shading", design.shadingStyle)
        edit.putInt("${key}_angle", design.angle)
        edit.putInt("${key}_opacity", design.opacity)
        edit.putInt("${key}_border_color", design.borderColor)
        edit.putInt("${key}_border_width", design.borderWidth)
        edit.putInt("${key}_radius", design.radius)
        edit.putFloat("${key}_shadow", design.shadow)
        edit.putBoolean("${key}_glass", design.liquidGlass)
        edit.apply()
    }

    private fun loadDesign(key: String): BoxDesign {
        val defaults = palettes[0]
        return BoxDesign(
            colors = IntArray(5) { i ->
                prefs.getInt("${key}_color_$i", defaults[i])
            },
            gradientStyle = prefs.getInt("${key}_gradient", 0),
            shadingStyle = prefs.getInt("${key}_shading", 0),
            angle = prefs.getInt("${key}_angle", 45),
            opacity = prefs.getInt("${key}_opacity", 100),
            borderColor = prefs.getInt("${key}_border_color", Color.WHITE),
            borderWidth = prefs.getInt("${key}_border_width", 1),
            radius = prefs.getInt("${key}_radius", 22),
            shadow = prefs.getFloat("${key}_shadow", 8f),
            liquidGlass = prefs.getBoolean("${key}_glass", false)
        )
    }

    fun applyTo(view: View, boxId: String) {
        val design = if (prefs.contains("box_${boxId}_gradient")) {
            loadDesign("box_$boxId")
        } else if (isApplyToAll() && prefs.contains("all_boxes_gradient")) {
            loadDesign("all_boxes")
        } else {
            BoxDesign(gradientColors(0))
        }

        val colors = design.colors.map { color ->
            val alpha = (Color.alpha(color) * design.opacity / 100)
                .coerceIn(0, 255)
            Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        }.toIntArray()

        val drawable = GradientDrawable(
            orientationFor(design.angle),
            colors
        ).apply {
            cornerRadius = design.radius.coerceAtLeast(0).toFloat()
            setStroke(
                design.borderWidth.coerceAtLeast(0),
                if (design.liquidGlass) 0x99FFFFFF.toInt()
                else design.borderColor
            )
        }

        view.background = drawable
        view.elevation = design.shadow.coerceIn(0f, 40f)
    }

    private fun orientationFor(angle: Int): GradientDrawable.Orientation {
        return when (((angle % 360) + 360) % 360) {
            in 0..22, in 338..359 -> GradientDrawable.Orientation.LEFT_RIGHT
            in 23..67 -> GradientDrawable.Orientation.BL_TR
            in 68..112 -> GradientDrawable.Orientation.BOTTOM_TOP
            in 113..157 -> GradientDrawable.Orientation.BR_TL
            in 158..202 -> GradientDrawable.Orientation.RIGHT_LEFT
            in 203..247 -> GradientDrawable.Orientation.TR_BL
            in 248..292 -> GradientDrawable.Orientation.TOP_BOTTOM
            else -> GradientDrawable.Orientation.TL_BR
        }
    }

    fun saveBoxColor(boxId: String, color: Int) {
        prefs.edit().putInt("box_color_$boxId", color).apply()
    }

    fun getBoxColor(boxId: String): Int =
        prefs.getInt("box_color_$boxId", 0xFF202D42.toInt())

    fun saveTextColor(color: Int) {
        prefs.edit().putInt("text_color", color).apply()
    }

    fun getTextColor(): Int =
        prefs.getInt("text_color", Color.WHITE)

    fun saveTextSize(size: Float) {
        prefs.edit().putFloat("text_size", size).apply()
    }

    fun getTextSize(): Float =
        prefs.getFloat("text_size", 18f)

    fun saveTextWeight(weight: Int) {
        prefs.edit().putInt("text_weight", weight).apply()
    }

    fun getTextWeight(): Int =
        prefs.getInt("text_weight", 700)

    fun saveTextStyle(style: Int) {
        prefs.edit().putInt("text_style", style).apply()
    }

    fun getTextStyle(): Int =
        prefs.getInt("text_style", 1)
}
