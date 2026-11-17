
package com.hisab.kitab

import android.content.ContentValues

data class PaymentEntry(
    val id: Long,
    val amount: Double,
    val note: String?,
    val createdAt: Long
)

class PaymentManager(private val db: AppDatabase) {

    fun addPayment(amount: Double, note: String? = null): Long {
        require(amount > 0)

        val database = db.writableDatabase
        val values = ContentValues().apply {
            put("amount", amount)
            put("note", note?.trim())
            put("created_at", System.currentTimeMillis())
        }

        database.beginTransaction()
        try {
            val id = database.insertOrThrow("payments", null, values)
            addAudit(
                database, id, "ADD",
                "पेमेंट जोड़ा: ₹$amount${note?.takeIf { it.isNotBlank() }?.let { " — $it" } ?: ""}"
            )
            database.setTransactionSuccessful()
            return id
        } finally {
            database.endTransaction()
        }
    }

    fun totalReceived(): Double {
        db.readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(amount),0) FROM payments", null
        ).use {
            return if (it.moveToFirst()) it.getDouble(0) else 0.0
        }
    }

    fun getAllPayments(): List<PaymentEntry> {
        val result = mutableListOf<PaymentEntry>()

        db.readableDatabase.rawQuery(
            "SELECT * FROM payments ORDER BY created_at DESC", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(
                    PaymentEntry(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("note")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        return result
    }

    fun updatePayment(
        id: Long,
        amount: Double,
        note: String? = null
    ): Boolean {
        require(amount > 0)

        val database = db.writableDatabase
        val values = ContentValues().apply {
            put("amount", amount)
            put("note", note?.trim())
        }

        database.beginTransaction()
        try {
            val changed = database.update(
                "payments", values, "id=?",
                arrayOf(id.toString())
            ) > 0

            if (changed) {
                addAudit(
                    database, id, "EDIT",
                    "पेमेंट बदला: ₹$amount${note?.takeIf { it.isNotBlank() }?.let { " — $it" } ?: ""}"
                )
            }

            database.setTransactionSuccessful()
            return changed
        } finally {
            database.endTransaction()
        }
    }

    fun deletePayment(id: Long): Boolean {
        val old = getAllPayments().firstOrNull { it.id == id } ?: return false
        val database = db.writableDatabase

        database.beginTransaction()
        try {
            val deleted = database.delete(
                "payments", "id=?", arrayOf(id.toString())
            ) > 0

            if (deleted) {
                addAudit(
                    database, id, "DELETE",
                    "पेमेंट हटाया: ₹${old.amount} ${old.note.orEmpty()}"
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
            put("item_type", "PAYMENT")
            put("item_id", id)
            put("action", action)
            put("description", description)
            put("created_at", System.currentTimeMillis())
        }
        database.insert("audit_history", null, values)
    }
}
