
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

        val values = ContentValues().apply {
            put("amount", amount)
            put("note", note)
            put("created_at", System.currentTimeMillis())
        }

        return db.writableDatabase.insertOrThrow("payments", null, values)
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

        val values = ContentValues().apply {
            put("amount", amount)
            put("note", note)
        }

        return db.writableDatabase.update(
            "payments", values, "id=?", arrayOf(id.toString())
        ) > 0
    }

    fun deletePayment(id: Long): Boolean =
        db.writableDatabase.delete(
            "payments", "id=?", arrayOf(id.toString())
        ) > 0
}
