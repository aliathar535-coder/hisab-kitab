
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
    fun dateText(): String {
        return SimpleDateFormat(
            "dd-MM-yyyy  hh:mm a",
            Locale.getDefault()
        ).format(Date(createdAt))
    }
}

class CalculatorHistoryManager(context: Context) {

    private val db = AppDatabase(context)

    fun save(expression: String, result: String): Long {
        if (expression.isBlank() || result.isBlank()) return -1L

        val values = ContentValues().apply {
            put("expression", expression)
            put("result", result)
            put("created_at", System.currentTimeMillis())
        }

        return db.writableDatabase.insert(
            "calculator_history",
            null,
            values
        )
    }

    fun getAll(): List<CalculatorHistoryItem> {
        val items = mutableListOf<CalculatorHistoryItem>()

        val cursor = db.readableDatabase.rawQuery(
            "SELECT id, expression, result, created_at " +
                "FROM calculator_history ORDER BY id DESC",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    CalculatorHistoryItem(
                        id = it.getLong(0),
                        expression = it.getString(1),
                        result = it.getString(2),
                        createdAt = it.getLong(3)
                    )
                )
            }
        }

        return items
    }

    fun delete(id: Long): Boolean {
        return db.writableDatabase.delete(
            "calculator_history",
            "id = ?",
            arrayOf(id.toString())
        ) > 0
    }

    fun clearAll() {
        db.writableDatabase.delete(
            "calculator_history",
            null,
            null
        )
    }

    fun close() {
        db.close()
    }
}
