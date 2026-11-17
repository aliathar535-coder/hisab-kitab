
package com.hisab.kitab

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context) {

    private val databaseFile: File
        get() = context.getDatabasePath("hisab_kitab.db")

    private val photoFolder: File
        get() = File(context.filesDir, "piece_photos")

    fun createBackup(): File? {
        return try {
            val database = databaseFile
            if (!database.exists()) return null

            database.parentFile?.mkdirs()

            // Flush pending WAL changes into the main database before copying.
            val checkpointDb = SQLiteDatabase.openDatabase(
                database.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE
            )

            try {
                checkpointDb.rawQuery(
                    "PRAGMA wal_checkpoint(FULL)",
                    null
                ).use { }
            } finally {
                checkpointDb.close()
            }

            val backup = File(
                context.cacheDir,
                "HISAB_BACKUP_${System.currentTimeMillis()}.zip"
            )

            ZipOutputStream(FileOutputStream(backup)).use { zip ->
                addFile(zip, database, "hisab_kitab.db")

                photoFolder.listFiles()
                    ?.filter { it.isFile }
                    ?.forEach { photo ->
                        addFile(zip, photo, "photos/${photo.name}")
                    }
            }

            backup
        } catch (_: Exception) {
            null
        }
    }

    private fun addFile(
        zip: ZipOutputStream,
        file: File,
        entryName: String
    ) {
        zip.putNextEntry(ZipEntry(entryName))
        FileInputStream(file).use { input ->
            input.copyTo(zip)
        }
        zip.closeEntry()
    }

    fun restoreBackup(uri: Uri): Boolean {
        val tempDatabase = File(
            context.cacheDir,
            "restore_${System.currentTimeMillis()}.db"
        )

        val tempPhotos = File(
            context.cacheDir,
            "restore_photos_${System.currentTimeMillis()}"
        )

        return try {
            if (!tempPhotos.mkdirs() && !tempPhotos.isDirectory) {
                return false
            }

            var databaseRestored = false
            var totalBytes = 0L
            val maxBackupBytes = 300L * 1024L * 1024L

            val input = context.contentResolver.openInputStream(uri)
                ?: return false

            input.use { stream ->
                ZipInputStream(stream).use { zip ->
                    var entry = zip.nextEntry

                    while (entry != null) {
                        if (!entry.isDirectory) {
                            val name = entry.name

                            when {
                                name == "hisab_kitab.db" -> {
                                    totalBytes += copyLimited(
                                        zip,
                                        tempDatabase,
                                        maxBackupBytes - totalBytes
                                    )
                                    databaseRestored = true
                                }

                                name.startsWith("photos/") -> {
                                    val photoName = name.removePrefix("photos/")

                                    if (
                                        photoName.isNotBlank() &&
                                        !photoName.contains("/") &&
                                        !photoName.contains("\\") &&
                                        photoName != "." &&
                                        photoName != ".."
                                    ) {
                                        val target = File(tempPhotos, photoName)
                                        totalBytes += copyLimited(
                                            zip,
                                            target,
                                            maxBackupBytes - totalBytes
                                        )
                                    }
                                }
                            }
                        }

                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }

            if (!databaseRestored || !tempDatabase.exists()) {
                return false
            }

            // Confirm that the restored file is a readable SQLite database.
            val checkDb = SQLiteDatabase.openDatabase(
                tempDatabase.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            try {
                checkDb.rawQuery(
                    "SELECT name FROM sqlite_master LIMIT 1",
                    null
                ).use { }
            } finally {
                checkDb.close()
            }

            val database = databaseFile
            database.parentFile?.mkdirs()

            // Remove old WAL sidecars before replacing the database.
            File(database.absolutePath + "-wal").delete()
            File(database.absolutePath + "-shm").delete()

            val replacement = File(database.parentFile, "hisab_kitab_restore.tmp")
            tempDatabase.copyTo(replacement, overwrite = true)

            if (database.exists() && !database.delete()) {
                replacement.delete()
                return false
            }

            if (!replacement.renameTo(database)) {
                replacement.copyTo(database, overwrite = true)
                replacement.delete()
            }

            if (!photoFolder.exists()) photoFolder.mkdirs()

            tempPhotos.listFiles()?.forEach { restoredPhoto ->
                restoredPhoto.copyTo(
                    File(photoFolder, restoredPhoto.name),
                    overwrite = true
                )
            }

            true
        } catch (_: Exception) {
            false
        } finally {
            tempDatabase.delete()
            tempPhotos.deleteRecursively()
        }
    }

    private fun copyLimited(
        input: ZipInputStream,
        target: File,
        remainingLimit: Long
    ): Long {
        require(remainingLimit > 0L) { "Backup is too large" }

        var copied = 0L
        val buffer = ByteArray(8192)

        FileOutputStream(target).use { output ->
            while (true) {
                val count = input.read(buffer)
                if (count == -1) break

                copied += count
                require(copied <= remainingLimit) {
                    "Backup is too large"
                }

                output.write(buffer, 0, count)
            }
        }

        return copied
    }

    fun getBackupFiles(): List<File> =
        context.cacheDir.listFiles()
            ?.filter {
                it.isFile &&
                    it.name.startsWith("HISAB_BACKUP_") &&
                    it.name.endsWith(".zip")
            }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
}
