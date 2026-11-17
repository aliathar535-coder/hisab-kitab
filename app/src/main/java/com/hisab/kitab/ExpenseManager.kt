
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

        val database = db.writableDatabase
        val values = ContentValues().apply {
            put("title", title.trim())
            put("amount", amount)
            put("created_at", System.currentTimeMillis())
        }

        database.beginTransaction()
        try {
            val id = database.insertOrThrow("expenses", null, values)
            addAudit(
                database, id, "ADD",
                "खर्च जोड़ा: ${title.trim()} — ₹$amount"
            )
            database.setTransactionSuccessful()
            return id
        } finally {
            database.endTransaction()
        }
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
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        return result
    }

    fun updateExpense(
        id: Long,
        title: String,
        amount: Double
    ): Boolean {
        require(title.isNotBlank() && amount > 0)

        val database = db.writableDatabase
        val values = ContentValues().apply {
            put("title", title.trim())
            put("amount", amount)
        }

        database.beginTransaction()
        try {
            val changed = database.update(
                "expenses", values, "id=?",
                arrayOf(id.toString())
            ) > 0

            if (changed) {
                addAudit(
                    database, id, "EDIT",
                    "खर्च बदला: ${title.trim()} — ₹$amount"
                )
            }

            database.setTransactionSuccessful()
            return changed
        } finally {
            database.endTransaction()
        }
    }

    fun deleteExpense(id: Long): Boolean {
        val old = getAllExpenses().firstOrNull { it.id == id } ?: return false
        val database = db.writableDatabase

        database.beginTransaction()
        try {
            val deleted = database.delete(
                "expenses", "id=?", arrayOf(id.toString())
            ) > 0

            if (deleted) {
                addAudit(
                    database, id, "DELETE",
                    "खर्च हटाया: ${old.title} — ₹${old.amount}"
                )
            }

            database.setTransactionSuccessful()
            return deleted
        } finally {
            database.endTransaction()
        }
    }

    private fun addAudit(
        database: android.database.sqlite.SQLiteDatabase,
        id: Long,
        action: String,
        description: String
    ) {
        val values = ContentValues().apply {
            put("item_type", "EXPENSE")
            put("item_id", id)
            put("action", action)
            put("description", description)
            put("created_at", System.currentTimeMillis())
        }
        database.insert("audit_history", null, values)
    }
}
