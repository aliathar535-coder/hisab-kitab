
package com.hisab.kitab

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportManager(private val context: Context) {

    fun createPdf(): File? {
        val db = AppDatabase(context)
        val pdf = PdfDocument()

        return try {
            val history = HistoryManager(db)
            val summary = history.getSummary()

            val production = history.getProductionHistory()
            val payments = history.getPaymentHistory()
            val expenses = history.getExpenseHistory()

            val pageWidth = 595
            val pageHeight = 842
            val left = 38f
            val right = 555f
            var pageNumber = 0
            var page = pdf.startPage(
                PdfDocument.PageInfo.Builder(
                    pageWidth, pageHeight, ++pageNumber
                ).create()
            )

            var canvas = page.canvas
            var y = 48f

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.BLACK
                textSize = 12f
            }

            fun newPage() {
                pdf.finishPage(page)
                page = pdf.startPage(
                    PdfDocument.PageInfo.Builder(
                        pageWidth, pageHeight, ++pageNumber
                    ).create()
                )
                canvas = page.canvas
                y = 48f
            }

            fun drawLine(
                value: String,
                size: Float = 12f,
                bold: Boolean = false
            ) {
                paint.textSize = size
                paint.typeface = if (bold) {
                    Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }

                val maxWidth = right - left
                val words = value.split(" ")
                var current = ""

                for (word in words) {
                    val candidate = if (current.isEmpty()) {
                        word
                    } else {
                        "$current $word"
                    }

                    if (
                        current.isNotEmpty() &&
                        paint.measureText(candidate) > maxWidth
                    ) {
                        if (y > 790f) newPage()
                        canvas.drawText(current, left, y, paint)
                        y += size + 7f
                        current = word
                    } else {
                        current = candidate
                    }
                }

                if (current.isNotEmpty()) {
                    if (y > 790f) newPage()
                    canvas.drawText(current, left, y, paint)
                    y += size + 9f
                }
            }

            fun separator() {
                if (y > 780f) newPage()
                paint.strokeWidth = 0.7f
                canvas.drawLine(left, y, right, y, paint)
                y += 16f
            }

            drawLine("HISAB-KITAB REPORT", 22f, true)
            drawLine(
                "Report Date: " + SimpleDateFormat(
                    "dd-MM-yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date()),
                10f
            )
            separator()

            drawLine("SUMMARY", 16f, true)
            drawLine("Total Pieces: ${summary.totalPieces}")
            drawLine("Total Income: ${history.formatMoney(summary.totalIncome)}")
            drawLine("Received Payment: ${history.formatMoney(summary.received)}")
            drawLine("Total Expenses: ${history.formatMoney(summary.expenses)}")
            drawLine("Remaining Amount: ${history.formatMoney(summary.remaining)}")
            drawLine("Profit / Loss: ${history.formatMoney(summary.profit)}")
            drawLine(
                "Profit / Loss Percentage: " +
                    String.format(Locale.US, "%.2f%%", summary.profitPercentage)
            )
            separator()

            drawLine("PRODUCTION HISTORY", 16f, true)
            if (production.isEmpty()) {
                drawLine("No production entries.")
            } else {
                production.forEach {
                    drawLine(
                        "${it.name} | ${it.quantity} x " +
                            "${history.formatMoney(it.rate)} = " +
                            history.formatMoney(it.total)
                    )
                    drawLine("Date: ${history.formatDate(it.createdAt)}", 9f)
                }
            }

            separator()
            drawLine("PAYMENT HISTORY", 16f, true)
            if (payments.isEmpty()) {
                drawLine("No payment entries.")
            } else {
                payments.forEach {
                    drawLine(
                        "${it.note.orEmpty().ifBlank { "Payment" }} | " +
                            history.formatMoney(it.amount)
                    )
                    drawLine("Date: ${history.formatDate(it.createdAt)}", 9f)
                }
            }

            separator()
            drawLine("EXPENSE HISTORY", 16f, true)
            if (expenses.isEmpty()) {
                drawLine("No expense entries.")
            } else {
                expenses.forEach {
                    drawLine(
                        "${it.title} | ${history.formatMoney(it.amount)}"
                    )
                    drawLine("Date: ${history.formatDate(it.createdAt)}", 9f)
                }
            }

            pdf.finishPage(page)

            val file = File(
                context.cacheDir,
                "HISAB_KITAB_${System.currentTimeMillis()}.pdf"
            )

            FileOutputStream(file).use { output ->
                pdf.writeTo(output)
            }

            file
        } catch (_: Exception) {
            null
        } finally {
            try {
                pdf.close()
            } catch (_: Exception) {
            }
            db.close()
        }
    }
}
