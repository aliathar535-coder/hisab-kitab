
package com.hisab.kitab

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

class PhotoManager(private val context: Context) {

    private val folder = File(
        context.filesDir,
        "piece_photos"
    ).apply {
        if (!exists()) mkdirs()
    }

    // Original photo bytes are copied without resizing or compression.
    fun savePhoto(uri: Uri): String? {
        var outputFile: File? = null

        return try {
            val mime = context.contentResolver.getType(uri)

            if (mime != null && !mime.startsWith("image/")) {
                return null
            }

            val extension = when (mime?.lowercase()) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                "image/heic" -> "heic"
                "image/heif" -> "heif"
                "image/jpeg", "image/jpg" -> "jpg"
                else -> "img"
            }

            outputFile = File(
                folder,
                "piece_${System.currentTimeMillis()}_${(1000..9999).random()}.$extension"
            )

            val input = context.contentResolver.openInputStream(uri)
                ?: return null

            input.use { source ->
                FileOutputStream(outputFile).use { destination ->
                    source.copyTo(destination)
                    destination.fd.sync()
                }
            }

            if (!outputFile.exists() || outputFile.length() <= 0L) {
                outputFile.delete()
                return null
            }

            outputFile.absolutePath
        } catch (e: Exception) {
            outputFile?.delete()
            null
        }
    }

    // Loads a memory-safe version for previews.
    // The original image file remains unchanged.
    fun loadPhoto(
        path: String,
        maxWidth: Int = 1200,
        maxHeight: Int = 1200
    ): Bitmap? {
        if (maxWidth <= 0 || maxHeight <= 0) return null

        return try {
            val file = File(path)
            if (!file.exists() || !file.isFile) return null

            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeFile(file.absolutePath, bounds)

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null
            }

            var sampleSize = 1
            while (
                bounds.outWidth / sampleSize > maxWidth ||
                bounds.outHeight / sampleSize > maxHeight
            ) {
                if (sampleSize > 1024) break
                sampleSize *= 2
            }

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: OutOfMemoryError) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun deletePhoto(path: String): Boolean {
        return try {
            val file = File(path).canonicalFile
            val safeFolder = folder.canonicalFile

            if (file.parentFile != safeFolder) {
                return false
            }

            file.exists() && file.isFile && file.delete()
        } catch (e: Exception) {
            false
        }
    }

    fun allPhotos(): List<File> {
        return folder.listFiles()
            ?.filter { it.isFile }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    fun photoExists(path: String?): Boolean {
        if (path.isNullOrBlank()) return false

        return try {
            val file = File(path).canonicalFile
            file.parentFile == folder.canonicalFile &&
                file.exists() && file.isFile
        } catch (e: Exception) {
            false
        }
    }
}
