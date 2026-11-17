package com.hisab.kitab

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class SettingsScreen(
    private val context: Context,
    private val onLanguageChanged: () -> Unit,
    private val onBack: () -> Unit
) {

    fun show(): LinearLayout {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 24, 20, 20)
            setBackgroundColor(0xFF101827.toInt())
            layoutParams = ViewGroup.LayoutParams(-1, -1)
        }

        root.addView(title(context.getString(R.string.settings), 28))
        root.addView(info(context.getString(R.string.settings_subtitle)))

        root.addView(title("PIECE MANAGEMENT", 20))
        root.addView(
            info("अपने काम के पीस का नाम और प्रति पीस रेट सेव करें।")
        )

        root.addView(actionButton("📦 SAVED PIECE & RATE") {
            val screen = PiecePresetScreen(context).show()

            AlertDialog.Builder(context)
                .setTitle("सेव किए हुए पीस और रेट")
                .setView(screen)
                .setPositiveButton("बंद", null)
                .show()
        })

        root.addView(title("APP CUSTOMIZATION", 20))
        root.addView(info("ऐप के डिजाइन और Font को अपनी पसंद से बदलें।"))

        root.addView(actionButton("🎨 APP DESIGN STUDIO") {
            AlertDialog.Builder(context)
                .setTitle("App Design")
                .setMessage(
                    "होम स्क्रीन के Design Studio से " +
                    "Liquid Glass, Neumorphic, Holographic और Metallic " +
                    "स्टाइल चुनें।"
                )
                .setPositiveButton("ठीक है", null)
                .show()
        })

        root.addView(actionButton("🔤 FONT STUDIO") {
            AlertDialog.Builder(context)
                .setTitle("Font Studio")
                .setMessage(
                    "होम स्क्रीन के Font Studio में Font Family, " +
                    "Bold, Italic, Shadow, Outline और Letter Spacing बदलें।"
                )
                .setPositiveButton("ठीक है", null)
                .show()
        })

        root.addView(title(context.getString(R.string.language), 20))
        root.addView(info(context.getString(R.string.language_info)))

        val selected = LanguageManager.getLanguage(context)

        root.addView(
            languageButton(
                context.getString(R.string.hindi),
                selected == "hi"
            ) {
                if (LanguageManager.getLanguage(context) != "hi") {
                    LanguageManager.setLanguage(context, "hi")
                    onLanguageChanged()
                }
            }
        )

        root.addView(
            languageButton(
                context.getString(R.string.english),
                selected == "en"
            ) {
                if (LanguageManager.getLanguage(context) != "en") {
                    LanguageManager.setLanguage(context, "en")
                    onLanguageChanged()
                }
            }
        )

        root.addView(info(context.getString(R.string.language_note)))
        root.addView(actionButton(context.getString(R.string.back_home), onBack))

        return root
    }

    private fun title(value: String, size: Int) =
        TextView(context).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 12, 0, 8)
        }

    private fun info(value: String) =
        TextView(context).apply {
            text = value
            textSize = 14f
            setTextColor(0xFFB8C5D9.toInt())
            setPadding(4, 8, 4, 14)
        }

    private fun actionButton(
        value: String,
        action: () -> Unit
    ) = Button(context).apply {
        text = value
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun languageButton(
        label: String,
        selected: Boolean,
        action: () -> Unit
    ): Button {
        return Button(context).apply {
            text = if (selected) "✓  $label" else label
            isAllCaps = false
            textSize = 17f
            setTextColor(Color.WHITE)

            background = GradientDrawable().apply {
                setColor(
                    if (selected) 0xFF164E63.toInt()
                    else 0xFF26364D.toInt()
                )
                cornerRadius = 20f
            }

            layoutParams = LinearLayout.LayoutParams(-1, 64).apply {
                bottomMargin = 12
            }

            setOnClickListener { action() }
        }
    }
}
