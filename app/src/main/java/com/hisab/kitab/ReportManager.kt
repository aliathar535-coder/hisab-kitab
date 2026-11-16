package com.hisab.kitab

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

class ReportManager(private val context: Context) {

    fun createPdf(): File? {
        return try {
            val history = HistoryManager(AppDatabase(context))
            val summary = history.getSummary()
            val pdf = PdfDocument()

            val pageInfo = PdfDocument.PageInfo.Builder(
                595, 842, 1
            ).create()

            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint().apply {
                textSize = 20f
                isAntiAlias = true
            }

            var y = 60f

            fun line(text: String, size: Float = 15f) {
                paint.textSize = size
                canvas.drawText(text, 40f, y, paint)
                y += size + 18f
            }

            line("HISAB-KITAB REPORT", 24f)
            line("Total Pieces: ${summary.totalPieces}")
            line("Total Income: ${history.formatMoney(summary.totalIncome)}")
            line("Received: ${history.formatMoney(summary.received)}")
            line("Expenses: ${history.formatMoney(summary.expenses)}")
            line("Remaining: ${history.formatMoney(summary.remaining)}")
            line("Profit/Loss: ${history.formatMoney(summary.profit)}")
            line(" ")
            line("Production History", 19f)

            history.getProductionHistory().take(25).forEach {
                line("${it.name}: ${it.quantity} x ${it.rate} = ${it.total}", 12f)
            }

            pdf.finishPage(page)

            val file = File(
                context.filesDir,
                "HisabKitab_Report_${System.currentTimeMillis()}.pdf"
            )

            FileOutputStream(file).use { pdf.writeTo(it) }
            pdf.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}
