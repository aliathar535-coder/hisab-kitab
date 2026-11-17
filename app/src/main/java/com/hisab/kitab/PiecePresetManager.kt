package com.hisab.kitab

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException

data class PiecePreset(
    val id: Long,
    val name: String,
    val rate: Double,
    val createdAt: Long
)

class PiecePresetManager(context: Context) {

    private val dbHelper = AppDatabase(context.applicationContext)

    fun addPreset(name: String, rate: Double): Long {
        val cleanName = name.trim()

        require(cleanName.isNotEmpty()) {
            "Piece का नाम खाली नहीं हो सकता"
        }
        require(rate >= 0.0 && rate.isFinite()) {
            "Rate सही डालें"
        }

        val values = ContentValues().apply {
            put("name", cleanName)
            put("rate", rate)
            put("created_at", System.currentTimeMillis())
        }

        return try {
            dbHelper.writableDatabase.insertOrThrow(
                "piece_presets",
                null,
                values
            )
        } catch (e: SQLiteConstraintException) {
            throw IllegalArgumentException(
                "इस नाम का Piece पहले से सेव है"
            )
        }
    }

    fun updatePreset(id: Long, name: String, rate: Double): Boolean {
        require(id > 0) { "गलत Preset ID" }

        val cleanName = name.trim()

        require(cleanName.isNotEmpty()) {
            "Piece का नाम खाली नहीं हो सकता"
        }
        require(rate >= 0.0 && rate.isFinite()) {
            "Rate सही डालें"
        }

        val values = ContentValues().apply {
            put("name", cleanName)
            put("rate", rate)
        }

        return try {
            dbHelper.writableDatabase.update(
                "piece_presets",
                values,
                "id = ?",
                arrayOf(id.toString())
            ) > 0
        } catch (e: SQLiteConstraintException) {
            throw IllegalArgumentException(
                "इस नाम का दूसरा Piece पहले से सेव है"
            )
        }
    }

    fun deletePreset(id: Long): Boolean {
        if (id <= 0) return false

        return dbHelper.writableDatabase.delete(
            "piece_presets",
            "id = ?",
            arrayOf(id.toString())
        ) > 0
    }

    fun getAllPresets(): List<PiecePreset> {
        val result = mutableListOf<PiecePreset>()

        val cursor = dbHelper.readableDatabase.query(
            "piece_presets",
            arrayOf("id", "name", "rate", "created_at"),
            null,
            null,
            null,
            null,
            "name COLLATE NOCASE ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                result.add(
                    PiecePreset(
                        id = it.getLong(0),
                        name = it.getString(1),
                        rate = it.getDouble(2),
                        createdAt = it.getLong(3)
                    )
                )
            }
        }

        return result
    }

    fun getPreset(id: Long): PiecePreset? {
        if (id <= 0) return null

        val cursor = dbHelper.readableDatabase.query(
            "piece_presets",
            arrayOf("id", "name", "rate", "created_at"),
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            "1"
        )

        cursor.use {
            if (!it.moveToFirst()) return null

            return PiecePreset(
                id = it.getLong(0),
                name = it.getString(1),
                rate = it.getDouble(2),
                createdAt = it.getLong(3)
            )
        }
    }

    fun findPresetByName(name: String): PiecePreset? {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return null

        val cursor = dbHelper.readableDatabase.query(
            "piece_presets",
            arrayOf("id", "name", "rate", "created_at"),
            "name = ? COLLATE NOCASE",
            arrayOf(cleanName),
            null,
            null,
            null,
            "1"
        )

        cursor.use {
            if (!it.moveToFirst()) return null

            return PiecePreset(
                id = it.getLong(0),
                name = it.getString(1),
                rate = it.getDouble(2),
                createdAt = it.getLong(3)
            )
        }
    }

    fun close() {
        dbHelper.close()
    }
}
