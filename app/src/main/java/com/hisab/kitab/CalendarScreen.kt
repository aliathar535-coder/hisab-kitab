
package com.hisab.kitab

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarScreen(private val context: Context) {

    fun show(): LinearLayout {
        val db = AppDatabase(context)

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        fun label(value: String, size: Float = 15f): TextView =
            TextView(context).apply {
                text = value
                textSize = size
                setTextColor(Color.WHITE)
                setPadding(10, 9, 10, 9)
            }

        val heading = label("📅 काम का कैलेंडर", 23f).apply {
            setTypeface(null, Typeface.BOLD)
        }

        val selectedDate = label("", 17f)
        val result = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val calendar = Calendar.getInstance()

        fun money(value: Double): String =
            "₹" + String.format(Locale.getDefault(), "%.2f", value)

        fun addSection(title: String) {
            result.addView(label(title, 17f).apply {
                setTypeface(null, Typeface.BOLD)
                setTextColor(0xFF38BDF8.toInt())
            })
        }

        fun addLine(value: String) {
            result.addView(label(value))
        }

        fun refresh() {
            val start = (calendar.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val end = (start.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }

            val from = start.timeInMillis
            val to = end.timeInMillis

            selectedDate.text = SimpleDateFormat(
                "EEEE, dd MMMM yyyy",
                Locale.getDefault()
            ).format(calendar.time)

            result.removeAllViews()

            val production = ProductionManager(db).getAllEntries()
                .filter { it.createdAt >= from && it.createdAt < to }

            val payments = PaymentManager(db).getAllPayments()
                .filter { it.createdAt >= from && it.createdAt < to }

            val expenses = ExpenseManager(db).getAllExpenses()
                .filter { it.createdAt >= from && it.createdAt < to }

            val pieces = production.sumOf { it.quantity }
            val income = production.sumOf { it.total }
            val received = payments.sumOf { it.amount }
            val spent = expenses.sumOf { it.amount }

            addSection("दिन का सारांश")
            addLine("कुल पीस: $pieces")
            addLine("कुल कमाई: ${money(income)}")
            addLine("मिली पेमेंट: ${money(received)}")
            addLine("कुल खर्च: ${money(spent)}")
            addLine("कमाई में से खर्च घटाकर: ${money(income - spent)}")

            addSection("पीस एंट्री")
            if (production.isEmpty()) addLine("इस दिन कोई पीस एंट्री नहीं।")
            production.forEach {
                addLine("${it.name} • ${it.quantity} पीस × ${money(it.rate)} = ${money(it.total)}")
            }

            addSection("पेमेंट")
            if (payments.isEmpty()) addLine("इस दिन कोई पेमेंट नहीं।")
            payments.forEach {
                addLine("${money(it.amount)} • ${it.note.orEmpty()}")
            }

            addSection("खर्च")
            if (expenses.isEmpty()) addLine("इस दिन कोई खर्च नहीं।")
            expenses.forEach {
                addLine("${it.title} • ${money(it.amount)}")
            }
        }

        val choose = Button(context).apply {
            text = "तारीख चुनें"
            isAllCaps = false
            setOnClickListener {
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        calendar.set(year, month, day)
                        refresh()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        val navigation = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
        }

        navigation.addView(Button(context).apply {
            text = "◀ पिछला दिन"
            isAllCaps = false
            setOnClickListener {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                refresh()
            }
        })

        navigation.addView(Button(context).apply {
            text = "आज"
            isAllCaps = false
            setOnClickListener {
                calendar.timeInMillis = System.currentTimeMillis()
                refresh()
            }
        })

        navigation.addView(Button(context).apply {
            text = "अगला दिन ▶"
            isAllCaps = false
            setOnClickListener {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                refresh()
            }
        })

        root.addView(heading)
        root.addView(selectedDate)
        root.addView(choose)
        root.addView(navigation)

        root.addView(ScrollView(context).apply {
            addView(result)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        })

        refresh()
        return root
    }
}
