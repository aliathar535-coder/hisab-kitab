
package com.hisab.kitab

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

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
        }

        val title = TextView(context).apply {
            text = context.getString(R.string.settings)
            textSize = 28f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        }

        root.addView(title)

        val subtitle = TextView(context).apply {
            text = context.getString(R.string.settings_subtitle)
            textSize = 14f
            setTextColor(0xFFB8C5D9.toInt())
            setPadding(0, 8, 0, 24)
        }

        root.addView(subtitle)

        val languageTitle = TextView(context).apply {
            text = context.getString(R.string.language)
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        }

        root.addView(languageTitle)

        val languageInfo = TextView(context).apply {
            text = context.getString(R.string.language_info)
            textSize = 13f
            setTextColor(0xFFB8C5D9.toInt())
            setPadding(0, 8, 0, 16)
        }

        root.addView(languageInfo)

        val selected = LanguageManager.getLanguage(context)

        val hindi = createLanguageButton(
            context.getString(R.string.hindi),
            selected == "hi"
        )

        hindi.setOnClickListener {
            if (selected != "hi") {
                LanguageManager.setLanguage(context, "hi")
                onLanguageChanged()
            }
        }

        root.addView(hindi)

        val english = createLanguageButton(
            context.getString(R.string.english),
            selected == "en"
        )

        english.setOnClickListener {
            if (selected != "en") {
                LanguageManager.setLanguage(context, "en")
                onLanguageChanged()
            }
        }

        root.addView(english)

        val note = TextView(context).apply {
            text = context.getString(R.string.language_note)
            textSize = 12f
            setTextColor(0xFF8FA3BF.toInt())
            setPadding(4, 16, 4, 20)
        }

        root.addView(note)

        val back = Button(context).apply {
            text = context.getString(R.string.back_home)
            isAllCaps = false
            setOnClickListener { onBack() }
        }

        root.addView(back)

        return root
    }

    private fun createLanguageButton(
        label: String,
        selected: Boolean
    ): Button {

        return Button(context).apply {
            text = if (selected) "✓  $label" else label
            isAllCaps = false
            textSize = 17f

            background = GradientDrawable().apply {
                setColor(
                    if (selected) 0xFF164E63.toInt()
                    else 0xFF26364D.toInt()
                )
                cornerRadius = 20f
            }

            setTextColor(Color.WHITE)

            layoutParams = LinearLayout.LayoutParams(
                -1,
                64
            ).apply {
                bottomMargin = 12
            }
        }
    }
}
