package com.hisab.kitab

import android.app.DatePickerDialog
import android.content.Context
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
            setPadding(18, 18, 18, 18)
        }

        val heading = TextView(context).apply {
            text = "📅 काम का कैलेंडर"
            textSize = 23f
            setPadding(0, 0, 0, 16)
        }

        val selectedDate = TextView(context).apply {
            textSize = 17f
            setPadding(0, 12, 0, 12)
        }

        val result = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val calendar = Calendar.getInstance()

        fun refresh() {
            val start = calendar.clone() as Calendar
            start.set(Calendar.HOUR_OF_DAY, 0)
            start.set(Calendar.MINUTE, 0)
            start.set(Calendar.SECOND, 0)
            start.set(Calendar.MILLISECOND, 0)
            val from = start.timeInMillis
            val to = from + 86400000L

            selectedDate.text = SimpleDateFormat(
                "EEEE, dd MMMM yyyy",
                Locale.getDefault()
            ).format(calendar.time)

            result.removeAllViews()

            val production = ProductionManager(db).getAllEntries()
                .filter { it.createdAt in from until to }

            val payments = PaymentManager(db).getAllPayments()
                .filter { it.createdAt in from until to }

            val expenses = ExpenseManager(db).getAllExpenses()
                .filter { it.createdAt in from until to }

            fun addLine(value: String) {
                result.addView(TextView(context).apply {
                    text = value
                    textSize = 15f
                    setPadding(10, 12, 10, 12)
                })
            }

            addLine("कुल पीस: ${production.sumOf { it.quantity }}")
            addLine("आज की कमाई: ₹${production.sumOf { it.total }}")
            addLine("मिली पेमेंट: ₹${payments.sumOf { it.amount }}")
            addLine("आज का खर्च: ₹${expenses.sumOf { it.amount }}")

            addLine("—— पीस एंट्री ——")
            if (production.isEmpty()) addLine("कोई पीस एंट्री नहीं")
            production.forEach {
                addLine("${it.name} • ${it.quantity} पीस • ₹${it.total}")
            }

            addLine("—— पेमेंट ——")
            if (payments.isEmpty()) addLine("कोई पेमेंट नहीं")
            payments.forEach {
                addLine("₹${it.amount} • ${it.note.orEmpty()}")
            }

            addLine("—— खर्च ——")
            if (expenses.isEmpty()) addLine("कोई खर्च नहीं")
            expenses.forEach {
                addLine("${it.title} • ₹${it.amount}")
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

        root.addView(heading)
        root.addView(selectedDate)
        root.addView(choose)
        root.addView(ScrollView(context).apply {
            addView(result)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        })

        refresh()
        return root
    }
}
