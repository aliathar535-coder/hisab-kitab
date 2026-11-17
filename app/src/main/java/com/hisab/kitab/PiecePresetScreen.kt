package com.hisab.kitab

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class PiecePresetScreen(private val context: Context) {

    private val manager = PiecePresetManager(context)

    private val backgroundColor = Color.rgb(16, 24, 39)
    private val cardColor = Color.rgb(30, 41, 59)
    private val textColor = Color.WHITE
    private val mutedColor = Color.rgb(184, 197, 217)
    private val accentColor = Color.rgb(56, 189, 248)

    private lateinit var listContainer: LinearLayout
    private lateinit var countText: TextView

    fun show(): View {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 20)
            setBackgroundColor(backgroundColor)
        }

        val heading = TextView(context).apply {
            text = "SAVED PIECE & RATE"
            textSize = 21f
            setTextColor(textColor)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(4, 12, 4, 8)
        }

        val description = TextView(context).apply {
            text = "अपने पीस के नाम और रेट सेव करें।"
            textSize = 14f
            setTextColor(mutedColor)
            setPadding(4, 4, 4, 14)
        }

        val addButton = Button(context).apply {
            text = "＋ नया पीस और रेट सेव करें"
            isAllCaps = false
            setOnClickListener {
                showAddDialog()
            }
        }

        countText = TextView(context).apply {
            textSize = 14f
            setTextColor(accentColor)
            setPadding(4, 16, 4, 8)
        }

        listContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

                val scroll = ScrollView(context).apply {
            isFillViewport = true
            addView(
                listContainer,
                android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )
                }
        root.addView(heading)
        root.addView(description)
        root.addView(addButton)
        root.addView(countText)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        refreshList()
        return root
    }

    private fun refreshList() {
        listContainer.removeAllViews()

        val presets = try {
            manager.getAllPresets()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "लिस्ट नहीं खुली: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            emptyList()
        }

        countText.text = "कुल सेव पीस: ${presets.size}"

        if (presets.isEmpty()) {
            val empty = TextView(context).apply {
                text = "अभी कोई पीस सेव नहीं है।\nऊपर दिए बटन से पहला पीस जोड़ें।"
                textSize = 15f
                setTextColor(mutedColor)
                gravity = Gravity.CENTER
                setPadding(16, 36, 16, 36)
            }
            listContainer.addView(empty)
            return
        }

        presets.forEach { preset ->
            val card = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 12, 16, 12)
                setBackgroundColor(cardColor)
                isClickable = true
                isFocusable = true
            }

            val name = TextView(context).apply {
                text = preset.name
                textSize = 18f
                setTextColor(textColor)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val rate = TextView(context).apply {
                text = "एक पीस का रेट: ₹${formatRate(preset.rate)}"
                textSize = 15f
                setTextColor(accentColor)
                setPadding(0, 6, 0, 8)
            }

            val actions = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
            }

            val editButton = Button(context).apply {
                text = "बदलें"
                isAllCaps = false
                setOnClickListener {
                    showEditDialog(preset)
                }
            }

            val deleteButton = Button(context).apply {
                text = "हटाएँ"
                isAllCaps = false
                setOnClickListener {
                    confirmDelete(preset)
                }
            }

            actions.addView(
                editButton,
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            actions.addView(
                deleteButton,
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            card.addView(name)
            card.addView(rate)
            card.addView(actions)

            card.setOnClickListener {
                showOptions(preset)
            }

            val params = LinearLayout.LayoutParams(-1, -2)
            params.bottomMargin = 12
            listContainer.addView(card, params)
        }
    }

    private fun showAddDialog() {
        val fields = createFields("", "")

        AlertDialog.Builder(context)
            .setTitle("नया पीस सेव करें")
            .setView(fields.first)
            .setNegativeButton("रद्द", null)
            .setPositiveButton("सेव", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener {
                            val name = fields.second.text.toString().trim()
                            val rate = fields.third.text.toString()
                                .toDoubleOrNull()

                            if (name.isEmpty()) {
                                fields.second.error = "पीस का नाम लिखें"
                                return@setOnClickListener
                            }

                            if (rate == null || rate < 0 || !rate.isFinite()) {
                                fields.third.error = "सही रेट लिखें"
                                return@setOnClickListener
                            }

                            try {
                                manager.addPreset(name, rate)
                                dialog.dismiss()
                                refreshList()
                                toast("पीस और रेट सेव हो गया।")
                            } catch (e: Exception) {
                                toast(e.message ?: "सेव नहीं हो सका।")
                            }
                        }
                }
                dialog.show()
            }
    }

    private fun showEditDialog(preset: PiecePreset) {
        val fields = createFields(
            preset.name,
            preset.rate.toString()
        )

        AlertDialog.Builder(context)
            .setTitle("पीस और रेट बदलें")
            .setView(fields.first)
            .setNegativeButton("रद्द", null)
            .setPositiveButton("अपडेट", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener {
                            val name = fields.second.text.toString().trim()
                            val rate = fields.third.text.toString()
                                .toDoubleOrNull()

                            if (name.isEmpty()) {
                                fields.second.error = "नाम लिखें"
                                return@setOnClickListener
                            }

                            if (rate == null || rate < 0 || !rate.isFinite()) {
                                fields.third.error = "सही रेट लिखें"
                                return@setOnClickListener
                            }

                            try {
                                val updated = manager.updatePreset(
                                    preset.id,
                                    name,
                                    rate
                                )

                                if (updated) {
                                    dialog.dismiss()
                                    refreshList()
                                    toast("पीस अपडेट हो गया।")
                                } else {
                                    toast("यह पीस नहीं मिला।")
                                }
                            } catch (e: Exception) {
                                toast(e.message ?: "अपडेट नहीं हुआ।")
                            }
                        }
                }
                dialog.show()
            }
    }

    private fun createFields(
        oldName: String,
        oldRate: String
    ): Triple<LinearLayout, EditText, EditText> {
        val box = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 8, 22, 8)
        }

        val name = EditText(context).apply {
            hint = "पीस का नाम"
            setText(oldName)
            singleLine = true
        }

        val rate = EditText(context).apply {
            hint = "एक पीस का रेट"
            inputType = 8194
            setText(oldRate)
            singleLine = true
        }

        box.addView(name)
        box.addView(rate)

        return Triple(box, name, rate)
    }

    private fun showOptions(preset: PiecePreset) {
        AlertDialog.Builder(context)
            .setTitle(preset.name)
            .setItems(arrayOf("रेट बदलें", "पीस हटाएँ")) { _, which ->
                when (which) {
                    0 -> showEditDialog(preset)
                    1 -> confirmDelete(preset)
                }
            }
            .setNegativeButton("बंद", null)
            .show()
    }

    private fun confirmDelete(preset: PiecePreset) {
        AlertDialog.Builder(context)
            .setTitle("पीस हटाएँ?")
            .setMessage(
                "${preset.name} का सेव किया हुआ नाम और रेट हट जाएगा।"
            )
            .setNegativeButton("नहीं", null)
            .setPositiveButton("हाँ, हटाएँ") { _, _ ->
                try {
                    if (manager.deletePreset(preset.id)) {
                        refreshList()
                        toast("पीस हटा दिया गया।")
                    } else {
                        toast("पीस नहीं मिला।")
                    }
                } catch (e: Exception) {
                    toast("हटाया नहीं जा सका: ${e.message}")
                }
            }
            .show()
    }

    private fun formatRate(value: Double): String {
        return String.format(
            java.util.Locale.getDefault(),
            "%.2f",
            value
        )
    }

    private fun toast(message: String) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
