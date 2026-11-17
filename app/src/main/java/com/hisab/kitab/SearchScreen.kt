
package com.hisab.kitab

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchScreen(
    private val context: Context,
    private val db: AppDatabase
) {

    private data class SearchItem(
        val type: String,
        val title: String,
        val detail: String,
        val timestamp: Long
    )

    fun show(): LinearLayout {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 14, 14, 14)
        }

        root.addView(TextView(context).apply {
            text = "🔍 हिसाब-किताब सर्च"
            textSize = 23f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(4, 8, 4, 16)
        })

        val search = EditText(context).apply {
            hint = "नाम, रकम, तारीख या विवरण लिखें"
            textSize = 16f
            setSingleLine(true)
            setPadding(12, 12, 12, 12)
        }

        val filter = Spinner(context).apply {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("सभी रिकॉर्ड", "पीस", "पेमेंट", "खर्च")
            )
        }

        val count = TextView(context).apply {
            textSize = 14f
            setTextColor(0xFF38BDF8.toInt())
            setPadding(4, 10, 4, 10)
        }

        val results = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val dateFormat = SimpleDateFormat(
            "dd-MM-yyyy hh:mm a",
            Locale.getDefault()
        )

        val allItems = mutableListOf<SearchItem>()

        ProductionManager(db).getAllEntries().forEach {
            allItems.add(
                SearchItem(
                    "पीस",
                    it.name,
                    "${it.quantity} पीस × ₹${it.rate} = ₹${it.total}",
                    it.createdAt
                )
            )
        }

        PaymentManager(db).getAllPayments().forEach {
            allItems.add(
                SearchItem(
                    "पेमेंट",
                    it.note.orEmpty().ifBlank { "पेमेंट प्राप्त" },
                    "मिली रकम: ₹${it.amount}",
                    it.createdAt
                )
            )
        }

        ExpenseManager(db).getAllExpenses().forEach {
            allItems.add(
                SearchItem(
                    "खर्च",
                    it.title,
                    "खर्च की रकम: ₹${it.amount}",
                    it.createdAt
                )
            )
        }

        allItems.sortByDescending { it.timestamp }

        fun refresh() {
            results.removeAllViews()

            val query = search.text.toString().trim()
            val selectedType = filter.selectedItem?.toString() ?: "सभी रिकॉर्ड"

            val filtered = allItems.filter { item ->
                val typeMatches = when (selectedType) {
                    "पीस" -> item.type == "पीस"
                    "पेमेंट" -> item.type == "पेमेंट"
                    "खर्च" -> item.type == "खर्च"
                    else -> true
                }

                val textMatches = query.isBlank() ||
                    (
                        item.type + " " +
                        item.title + " " +
                        item.detail + " " +
                        dateFormat.format(Date(item.timestamp))
                    ).contains(query, ignoreCase = true)

                typeMatches && textMatches
            }

            count.text = "कुल ${filtered.size} रिकॉर्ड मिले"

            if (filtered.isEmpty()) {
                results.addView(TextView(context).apply {
                    text = "कोई रिकॉर्ड नहीं मिला।\nदूसरा नाम या रकम लिखकर देखें।"
                    textSize = 16f
                    setTextColor(Color.WHITE)
                    setPadding(10, 24, 10, 24)
                })
            }

            filtered.forEach { item ->
                val card = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(12, 10, 12, 10)
                    background = GradientDrawable().apply {
                        setColor(0xFF1E293B.toInt())
                        cornerRadius = 18f
                        setStroke(1, 0x44FFFFFF)
                    }
                    layoutParams = LinearLayout.LayoutParams(
                        -1, -2
                    ).apply {
                        bottomMargin = 10
                    }
                }

                card.addView(TextView(context).apply {
                    text = "${item.type} • ${item.title}"
                    textSize = 16f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.WHITE)
                })

                card.addView(TextView(context).apply {
                    text = item.detail
                    textSize = 14f
                    setTextColor(0xFF38BDF8.toInt())
                    setPadding(0, 6, 0, 6)
                })

                card.addView(TextView(context).apply {
                    text = dateFormat.format(Date(item.timestamp))
                    textSize = 12f
                    setTextColor(0xFFCBD5E1.toInt())
                })

                results.addView(card)
            }
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                refresh()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        filter.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    refresh()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {}
            }

        root.addView(search)
        root.addView(filter)
        root.addView(count)

        root.addView(ScrollView(context).apply {
            addView(results)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        })

        refresh()
        return root
    }
}
