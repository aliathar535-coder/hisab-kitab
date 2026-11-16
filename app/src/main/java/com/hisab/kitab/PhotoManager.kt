package com.hisab.kitab

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

class PhotoManager(private val context: Context) {

    private val folder = File(context.filesDir, "piece_photos").apply {
        if (!exists()) mkdirs()
    }

    fun savePhoto(uri: Uri): String? {
        return try {
            val name = "piece_${System.currentTimeMillis()}.jpg"
            val file = File(folder, name)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun loadPhoto(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }

    fun deletePhoto(path: String): Boolean {
        return try {
            File(path).delete()
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
