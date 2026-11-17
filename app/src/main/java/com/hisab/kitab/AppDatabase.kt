
package com.hisab.kitab

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase(context: Context) :
    SQLiteOpenHelper(context, "hisab_kitab.db", null, 4) {

    override fun onCreate(db: SQLiteDatabase) {
        createProduction(db)
        createPayments(db)
        createExpenses(db)
        createCalculatorHistory(db)
        createAuditHistory(db)
        createPiecePresets(db)
    }

    private fun createProduction(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS production (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                rate REAL NOT NULL,
                total REAL NOT NULL,
                photo TEXT,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())
    }

    private fun createPayments(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS payments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount REAL NOT NULL,
                note TEXT,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())
    }

    private fun createExpenses(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                amount REAL NOT NULL,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())
    }

    private fun createCalculatorHistory(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS calculator_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                expression TEXT NOT NULL,
                result TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())
    }

    private fun createAuditHistory(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS audit_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                item_type TEXT NOT NULL,
                item_id INTEGER NOT NULL,
                action TEXT NOT NULL,
                description TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_audit_created
            ON audit_history(created_at)
        """.trimIndent())
    }

    private fun createPiecePresets(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS piece_presets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL COLLATE NOCASE UNIQUE,
                rate REAL NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            createCalculatorHistory(db)
        }

        if (oldVersion < 3) {
            createAuditHistory(db)
        }

        if (oldVersion < 4) {
            createPiecePresets(db)
        }
    }
}
