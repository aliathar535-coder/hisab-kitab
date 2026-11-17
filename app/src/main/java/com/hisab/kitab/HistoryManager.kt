
package com.hisab.kitab

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryManager(private val db: AppDatabase) {

    fun getProductionHistory(): List<ProductionEntry> =
        ProductionManager(db).getAllEntries()

    fun getPaymentHistory(): List<PaymentEntry> =
        PaymentManager(db).getAllPayments()

    fun getExpenseHistory(): List<ExpenseEntry> =
        ExpenseManager(db).getAllExpenses()

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        ).format(Date(timestamp))
    }

    fun formatMoney(amount: Double): String {
        return "₹" + String.format(Locale.getDefault(), "%.2f", amount)
    }

    fun getSummary(): Summary {
        val production = ProductionManager(db)
        val payments = PaymentManager(db)
        val expenses = ExpenseManager(db)

        val income = production.totalAmount()
        val received = payments.totalReceived()
        val spent = expenses.totalExpenses()
        val profit = income - spent

        val percentage = if (income > 0.0) {
            (profit / income) * 100.0
        } else {
            0.0
        }

        return Summary(
            totalPieces = production.totalQuantity(),
            totalIncome = income,
            received = received,
            expenses = spent,
            remaining = income - received,
            profit = profit,
            profitPercentage = percentage
        )
    }
}

data class Summary(
    val totalPieces: Int,
    val totalIncome: Double,
    val received: Double,
    val expenses: Double,
    val remaining: Double,
    val profit: Double,
    val profitPercentage: Double
)
