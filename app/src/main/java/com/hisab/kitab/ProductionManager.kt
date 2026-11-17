
package com.hisab.kitab

import android.content.ContentValues
import android.database.Cursor

data class ProductionEntry(
    val id: Long,
    val name: String,
    val quantity: Int,
    val rate: Double,
    val total: Double,
    val photo: String?,
    val createdAt: Long
)

class ProductionManager(private val db: AppDatabase) {

    fun addEntry(
        name: String,
        quantity: Int,
        rate: Double,
        photo: String? = null
    ): Long {
        require(name.isNotBlank() && quantity > 0 && rate >= 0)

        val values = ContentValues().apply {
            put("name", name.trim())
            put("quantity", quantity)
            put("rate", rate)
            put("total", quantity * rate)
            put("photo", photo)
            put("created_at", System.currentTimeMillis())
        }

        return db.writableDatabase.insertOrThrow("production", null, values)
    }

    fun getAllEntries(): List<ProductionEntry> {
        val result = mutableListOf<ProductionEntry>()

        db.readableDatabase.rawQuery(
            "SELECT * FROM production ORDER BY created_at DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(cursor.toEntry())
            }
        }
        return result
    }

    fun getEntry(id: Long): ProductionEntry? {
        db.readableDatabase.rawQuery(
            "SELECT * FROM production WHERE id=?",
            arrayOf(id.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toEntry() else null
        }
    }

    fun totalQuantity(): Int =
        scalar("COALESCE(SUM(quantity),0)").toInt()

    fun totalAmount(): Double =
        scalar("COALESCE(SUM(total),0)")

    private fun scalar(expression: String): Double {
        db.readableDatabase.rawQuery(
            "SELECT $expression FROM production", null
        ).use {
            return if (it.moveToFirst()) it.getDouble(0) else 0.0
        }
    }

    fun updateEntry(
        id: Long,
        name: String,
        quantity: Int,
        rate: Double,
        photo: String? = null
    ): Boolean {
        require(name.isNotBlank() && quantity > 0 && rate >= 0)

        val values = ContentValues().apply {
            put("name", name.trim())
            put("quantity", quantity)
            put("rate", rate)
            put("total", quantity * rate)
            put("photo", photo)
        }

        return db.writableDatabase.update(
            "production",
            values,
            "id=?",
            arrayOf(id.toString())
        ) > 0
    }

    fun updateDetails(
        id: Long,
        name: String,
        quantity: Int,
        rate: Double
    ): Boolean {
        require(name.isNotBlank() && quantity > 0 && rate >= 0)

        val values = ContentValues().apply {
            put("name", name.trim())
            put("quantity", quantity)
            put("rate", rate)
            put("total", quantity * rate)
        }

        return db.writableDatabase.update(
            "production", values, "id=?", arrayOf(id.toString())
        ) > 0
    }

    fun updatePhoto(id: Long, photo: String?): Boolean {
        val values = ContentValues().apply {
            put("photo", photo)
        }
        return db.writableDatabase.update(
            "production", values, "id=?", arrayOf(id.toString())
        ) > 0
    }

    fun deleteEntry(id: Long): Boolean =
        db.writableDatabase.delete(
            "production", "id=?", arrayOf(id.toString())
        ) > 0

    private fun Cursor.toEntry() = ProductionEntry(
        id = getLong(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        quantity = getInt(getColumnIndexOrThrow("quantity")),
        rate = getDouble(getColumnIndexOrThrow("rate")),
        total = getDouble(getColumnIndexOrThrow("total")),
        photo = getString(getColumnIndexOrThrow("photo")),
        createdAt = getLong(getColumnIndexOrThrow("created_at"))
    )
}
