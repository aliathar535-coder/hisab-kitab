
package com.hisab.kitab

import android.content.Context
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
            setPadding(4, 8, 4, 16)
        })

        val search = EditText(context).apply {
            hint = "नाम, रकम, तारीख या विवरण लिखें"
            textSize = 16f
            setSingleLine(true)
        }

        val count = TextView(context).apply {
            textSize = 14f
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
                    "पीस: ${it.name}",
                    "${it.quantity} पीस × ₹${it.rate} = ₹${it.total}",
                    it.createdAt
                )
            )
        }

        PaymentManager(db).getAllPayments().forEach {
            allItems.add(
                SearchItem(
                    "पेमेंट: ${it.note.orEmpty()}",
                    "मिली रकम: ₹${it.amount}",
                    it.createdAt
                )
            )
        }

        ExpenseManager(db).getAllExpenses().forEach {
            allItems.add(
                SearchItem(
                    "खर्च: ${it.title}",
                    "खर्च की रकम: ₹${it.amount}",
                    it.createdAt
                )
            )
        }

        allItems.sortByDescending { it.timestamp }

        fun refresh(query: String) {
            results.removeAllViews()

            val filtered = if (query.isBlank()) {
                allItems
            } else {
                allItems.filter {
                    (
                        it.title + " " +
                            it.detail + " " +
                            dateFormat.format(Date(it.timestamp))
                    ).contains(query, ignoreCase = true)
                }
            }

            count.text = "कुल ${filtered.size} रिकॉर्ड मिले"

            if (filtered.isEmpty()) {
                results.addView(TextView(context).apply {
                    text = "कोई रिकॉर्ड नहीं मिला।"
                    textSize = 16f
                    setPadding(10, 20, 10, 20)
                })
            }

            filtered.forEach { item ->
                val card = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(12, 10, 12, 10)
                    background = android.graphics.drawable.GradientDrawable().apply {
                        setColor(0xFF1E293B.toInt())
                        cornerRadius = 18f
                    }
                    layoutParams = LinearLayout.LayoutParams(
                        -1,
                        -2
                    ).apply {
                        bottomMargin = 10
                    }
                }

                card.addView(TextView(context).apply {
                    text = item.title
                    textSize = 16f
                    setTextColor(0xFFFFFFFF.toInt())
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
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                refresh(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        root.addView(search)
        root.addView(count)

        root.addView(ScrollView(context).apply {
            addView(results)
            layoutParams = LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        })

        refresh("")
        return root
    }
}
