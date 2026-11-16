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

        return db.writableDatabase.insert("payments", null, values)
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
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        note = cursor.getString(cursor.getColumnIndexOrThrow("note")),
                        createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }

        return result
    }

    fun deletePayment(id: Long): Boolean {
        return db.writableDatabase.delete(
            "payments", "id=?", arrayOf(id.toString())
        ) > 0
    }
}
