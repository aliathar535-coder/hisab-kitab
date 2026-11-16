package com.hisab.kitab

import android.content.ContentValues

data class ExpenseEntry(
    val id: Long,
    val title: String,
    val amount: Double,
    val createdAt: Long
)

class ExpenseManager(private val db: AppDatabase) {

    fun addExpense(title: String, amount: Double): Long {
        require(title.isNotBlank() && amount > 0)

        val values = ContentValues().apply {
            put("title", title.trim())
            put("amount", amount)
            put("created_at", System.currentTimeMillis())
        }

        return db.writableDatabase.insert("expenses", null, values)
    }

    fun totalExpenses(): Double {
        db.readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(amount),0) FROM expenses", null
        ).use {
            return if (it.moveToFirst()) it.getDouble(0) else 0.0
        }
    }

    fun getAllExpenses(): List<ExpenseEntry> {
        val result = mutableListOf<ExpenseEntry>()

        db.readableDatabase.rawQuery(
            "SELECT * FROM expenses ORDER BY created_at DESC", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(
                    ExpenseEntry(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }

        return result
    }

    fun deleteExpense(id: Long): Boolean {
        return db.writableDatabase.delete(
            "expenses", "id=?", arrayOf(id.toString())
        ) > 0
    }
}
