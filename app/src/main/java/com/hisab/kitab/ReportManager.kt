package com.hisab.kitab

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportManager(private val context: Context) {

    fun createPdf(): File? {
        return try {
            val db = AppDatabase(context)
            val history = HistoryManager(db)
            val summary = history.getSummary()
            val pdf = PdfDocument()

            val production = history.getProductionHistory()
            val payments = history.getPaymentHistory()
            val expenses = history.getExpenseHistory()

            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 0
            var page = pdf.startPage(
                PdfDocument.PageInfo.Builder(
                    pageWidth, pageHeight, ++pageNumber
                ).create()
            )

            var canvas = page.canvas
            val paint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.BLACK
                textSize = 13f
            }

            var y = 48f

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

            fun line(value: String, size: Float = 13f) {
                if (y > 790f) newPage()
                paint.textSize = size
                canvas.drawText(
                    value.take(85),
                    35f,
                    y,
                    paint
                )
                y += size + 12f
            }

            line("HISAB-KITAB REPORT", 23f)
            line("Date: " + SimpleDateFormat(
                "dd-MM-yyyy HH:mm",
                Locale.getDefault()
            ).format(Date()), 11f)

            line("----------------------------------------")
            line("TOTAL PIECES: ${summary.totalPieces}")
            line("TOTAL INCOME: ${history.formatMoney(summary.totalIncome)}")
            line("RECEIVED: ${history.formatMoney(summary.received)}")
            line("EXPENSES: ${history.formatMoney(summary.expenses)}")
            line("REMAINING: ${history.formatMoney(summary.remaining)}")
            line("PROFIT / LOSS: ${history.formatMoney(summary.profit)}")
            line("PROFIT %: ${"%.2f".format(summary.profitPercentage)}")
            line("----------------------------------------")

            line("PRODUCTION HISTORY", 18f)
            production.forEach {
                line("${it.name} | ${it.quantity} x ${it.rate} = ${it.total}")
            }

            line("----------------------------------------")
            line("PAYMENT HISTORY", 18f)
            payments.forEach {
                line("${it.note.orEmpty()} | Rs. ${it.amount}")
            }

            line("----------------------------------------")
            line("EXPENSE HISTORY", 18f)
            expenses.forEach {
                line("${it.title} | Rs. ${it.amount}")
            }

            pdf.finishPage(page)

            val file = File(
                context.cacheDir,
                "HISAB_KITAB_${System.currentTimeMillis()}.pdf"
            )

            FileOutputStream(file).use {
                pdf.writeTo(it)
            }

            pdf.close()
            db.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}
