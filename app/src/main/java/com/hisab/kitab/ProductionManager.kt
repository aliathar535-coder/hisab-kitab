
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

        val database = db.writableDatabase
        val values = ContentValues().apply {
            put("name", name.trim())
            put("quantity", quantity)
            put("rate", rate)
            put("total", quantity * rate)
            put("photo", photo)
            put("created_at", System.currentTimeMillis())
        }

        database.beginTransaction()
        try {
            val id = database.insertOrThrow("production", null, values)
            addAudit(
                database, id, "ADD",
                "पीस जोड़ा: ${name.trim()}, मात्रा $quantity, रेट $rate, कुल ${quantity * rate}"
            )
            database.setTransactionSuccessful()
            return id
        } finally {
            database.endTransaction()
        }
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

        val database = db.writableDatabase
        val values = ContentValues().apply {
            put("name", name.trim())
            put("quantity", quantity)
            put("rate", rate)
            put("total", quantity * rate)
            put("photo", photo)
        }

        database.beginTransaction()
        try {
            val changed = database.update(
                "production", values, "id=?",
                arrayOf(id.toString())
            ) > 0

            if (changed) {
                addAudit(
                    database, id, "EDIT",
                    "पीस बदला: ${name.trim()}, मात्रा $quantity, रेट $rate, कुल ${quantity * rate}"
                )
            }

            database.setTransactionSuccessful()
            return changed
        } finally {
            database.endTransaction()
        }
    }

    fun updateDetails(
        id: Long,
        name: String,
        quantity: Int,
        rate: Double
    ): Boolean {
        require(name.isNotBlank() && quantity > 0 && rate >= 0)

        val database = db.writableDatabase
        val values = ContentValues().apply {
            put("name", name.trim())
            put("quantity", quantity)
            put("rate", rate)
            put("total", quantity * rate)
        }

        database.beginTransaction()
        try {
            val changed = database.update(
                "production", values, "id=?",
                arrayOf(id.toString())
            ) > 0

            if (changed) {
                addAudit(
                    database, id, "EDIT",
                    "पीस विवरण बदला: ${name.trim()}, मात्रा $quantity, रेट $rate"
                )
            }

            database.setTransactionSuccessful()
            return changed
        } finally {
            database.endTransaction()
        }
    }

    fun updatePhoto(id: Long, photo: String?): Boolean {
        val database = db.writableDatabase
        val values = ContentValues().apply {
            put("photo", photo)
        }

        val changed = database.update(
            "production", values, "id=?",
            arrayOf(id.toString())
        ) > 0

        if (changed) {
            addAudit(
                database, id, "PHOTO",
                if (photo.isNullOrBlank()) "पीस की फोटो हटाई" else "पीस की फोटो बदली"
            )
        }
        return changed
    }

    fun deleteEntry(id: Long): Boolean {
        val database = db.writableDatabase
        val old = getEntry(id) ?: return false

        database.beginTransaction()
        try {
            val deleted = database.delete(
                "production", "id=?", arrayOf(id.toString())
            ) > 0

            if (deleted) {
                addAudit(
                    database, id, "DELETE",
                    "पीस हटाया: ${old.name}, मात्रा ${old.quantity}, कुल ${old.total}"
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
            put("item_type", "PRODUCTION")
            put("item_id", id)
            put("action", action)
            put("description", description)
            put("created_at", System.currentTimeMillis())
        }
        database.insert("audit_history", null, values)
    }

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
