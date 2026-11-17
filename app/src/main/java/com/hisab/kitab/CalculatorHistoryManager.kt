
package com.hisab.kitab

import android.content.ContentValues
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CalculatorHistoryItem(
    val id: Long,
    val expression: String,
    val result: String,
    val createdAt: Long
) {
    fun dateText(): String =
        SimpleDateFormat(
            "dd-MM-yyyy  hh:mm a",
            Locale.getDefault()
        ).format(Date(createdAt))
}

class CalculatorHistoryManager(context: Context) {

    private val db = AppDatabase(context.applicationContext)

    fun save(expression: String, result: String): Long {
        if (expression.isBlank() || result.isBlank()) return -1L

        val values = ContentValues().apply {
            put("expression", expression.trim())
            put("result", result.trim())
            put("created_at", System.currentTimeMillis())
        }

        return db.writableDatabase.insert(
            "calculator_history", null, values
        )
    }

    fun getAll(): List<CalculatorHistoryItem> {
        val items = mutableListOf<CalculatorHistoryItem>()

        db.readableDatabase.rawQuery(
            "SELECT id, expression, result, created_at " +
                "FROM calculator_history ORDER BY created_at DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                items.add(
                    CalculatorHistoryItem(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getLong(3)
                    )
                )
            }
        }
        return items
    }

    fun search(query: String): List<CalculatorHistoryItem> {
        if (query.isBlank()) return getAll()

        return getAll().filter {
            it.expression.contains(query, true) ||
                it.result.contains(query, true)
        }
    }

    fun delete(id: Long): Boolean =
        db.writableDatabase.delete(
            "calculator_history",
            "id=?",
            arrayOf(id.toString())
        ) > 0

    fun clearAll(): Int =
        db.writableDatabase.delete(
            "calculator_history", null, null
        )

    fun close() {
        db.close()
    }
}
