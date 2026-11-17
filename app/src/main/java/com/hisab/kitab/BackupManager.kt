package com.hisab.kitab

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context) {

    fun createBackup(): File? {
        return try {
            val database = context.getDatabasePath("hisab_kitab.db")
            if (!database.exists()) return null

            val backup = File(
                context.cacheDir,
                "HISAB_BACKUP_${System.currentTimeMillis()}.zip"
            )

            ZipOutputStream(FileOutputStream(backup)).use { zip ->
                zip.putNextEntry(ZipEntry("hisab_kitab.db"))
                FileInputStream(database).use { it.copyTo(zip) }
                zip.closeEntry()

                val photoFolder = File(context.filesDir, "piece_photos")
                photoFolder.listFiles()?.filter { it.isFile }?.forEach { photo ->
                    zip.putNextEntry(ZipEntry("photos/${photo.name}"))
                    FileInputStream(photo).use { it.copyTo(zip) }
                    zip.closeEntry()
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
            val photoFolder = File(context.filesDir, "piece_photos")
            if (!photoFolder.exists()) photoFolder.mkdirs()

            var databaseRestored = false

            context.contentResolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input).use { zip ->
                    var entry = zip.nextEntry

                    while (entry != null) {
                        if (!entry.isDirectory) {
                            when {
                                entry.name == "hisab_kitab.db" -> {
                                    FileOutputStream(database).use {
                                        zip.copyTo(it)
                                    }
                                    databaseRestored = true
                                }

                                entry.name.startsWith("photos/") -> {
                                    val name = entry.name
                                        .removePrefix("photos/")
                                    if (
                                        name.isNotBlank() &&
                                        !name.contains("/") &&
                                        !name.contains("\\") &&
                                        name != "." &&
                                        name != ".."
                                    ) {
                                        val target = File(photoFolder, name)
                                        FileOutputStream(target).use {
                                            zip.copyTo(it)
                                        }
                                    }
                                }
                            }
                        }

                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            } ?: return false

            databaseRestored
        } catch (e: Exception) {
            false
        }
    }

    fun getBackupFiles(): List<File> {
        return context.cacheDir.listFiles()
            ?.filter {
                it.name.startsWith("HISAB_BACKUP_") &&
                    it.name.endsWith(".zip")
            } ?: emptyList()
    }
}
