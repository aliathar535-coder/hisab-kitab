package com.hisab.kitab

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BackupManager(private val context: Context) {

    fun createBackup(): File? {
        return try {
            val source = context.getDatabasePath("hisab_kitab.db")
            if (!source.exists()) return null

            val backup = File(
                context.filesDir,
                "HisabKitab_Backup_${System.currentTimeMillis()}.db"
            )

            FileInputStream(source).use { input ->
                FileOutputStream(backup).use { output ->
                    input.copyTo(output)
                }
            }

            backup
        } catch (e: Exception) {
            null
        }
    }

    fun restoreBackup(uri: Uri): Boolean {
        return try {
            val database = context.getDatabasePath("hisab_kitab.db")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(database).use { output ->
                    input.copyTo(output)
                }
            } ?: return false

            true
        } catch (e: Exception) {
            false
        }
    }

    fun getBackupFiles(): List<File> {
        return context.filesDir.listFiles()
            ?.filter {
                it.name.startsWith("HisabKitab_Backup_")
            } ?: emptyList()
    }
}
