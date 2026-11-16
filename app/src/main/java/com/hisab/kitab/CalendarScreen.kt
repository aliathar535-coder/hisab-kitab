package com.hisab.kitab

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarScreen(private val context: Context) {

    fun show(): LinearLayout {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(0xFF101827.toInt())
        }

        val heading = TextView(context).apply {
            text = "काम का कैलेंडर"
            textSize = 24f
            setTextColor(0xFFFFFFFF.toInt())
        }

        val selectedDate = TextView(context).apply {
            textSize = 18f
            setTextColor(0xFF38BDF8.toInt())
            setPadding(0, 20, 0, 20)
            text = dateText(Calendar.getInstance())
        }

        val button = Button(context).apply {
            text = "तारीख चुनें"
            isAllCaps = false
            setOnClickListener {
                val calendar = Calendar.getInstance()

                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        calendar.set(year, month, day)
                        selectedDate.text = dateText(calendar)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        root.addView(heading)
        root.addView(selectedDate)
        root.addView(button)

        return root
    }

    private fun dateText(calendar: Calendar): String {
        return SimpleDateFormat(
            "EEEE, dd MMMM yyyy",
            Locale.getDefault()
        ).format(calendar.time)
    }
}
