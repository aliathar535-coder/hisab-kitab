
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

    // Original photo is copied without resizing or compression.
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
                else -> "jpg"
            }

            outputFile = File(
                folder,
                "piece_${System.currentTimeMillis()}.$extension"
            )

            val input = context.contentResolver.openInputStream(uri)
                ?: return null

            input.use { source ->
                FileOutputStream(outputFile).use { destination ->
                    source.copyTo(destination)
                }
            }

            if (outputFile.length() <= 0L) {
                outputFile.delete()
                return null
            }

            outputFile.absolutePath
        } catch (e: Exception) {
            outputFile?.delete()
            null
        }
    }

    fun loadPhoto(
        path: String,
        maxWidth: Int = 1200,
        maxHeight: Int = 1200
    ): Bitmap? {
        return try {
            val file = File(path)
            if (!file.exists()) return null

            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeFile(path, bounds)

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null
            }

            var sampleSize = 1
            while (
                bounds.outWidth / sampleSize > maxWidth ||
                bounds.outHeight / sampleSize > maxHeight
            ) {
                sampleSize *= 2
            }

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            BitmapFactory.decodeFile(path, options)
        } catch (e: OutOfMemoryError) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun deletePhoto(path: String): Boolean {
        return try {
            val file = File(path)
            file.exists() && file.delete()
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
}
