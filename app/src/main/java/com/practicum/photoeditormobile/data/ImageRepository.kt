package com.practicum.photoeditormobile.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

object ImageRepository {
    
    fun saveImageToGallery(
        bitmap: Bitmap,
        context: Context,
        format: ExportFormat = ExportFormat.JPEG,
        quality: Int = 95
    ): Boolean {
        return try {
            val extension = when (format) {
                ExportFormat.JPEG -> "jpg"
                ExportFormat.PNG -> "png"
                ExportFormat.WEBP -> "webp"
            }
            val mimeType = when (format) {
                ExportFormat.JPEG -> "image/jpeg"
                ExportFormat.PNG -> "image/png"
                ExportFormat.WEBP -> "image/webp"
            }
            val compressFormat = when (format) {
                ExportFormat.JPEG -> Bitmap.CompressFormat.JPEG
                ExportFormat.PNG -> Bitmap.CompressFormat.PNG
                ExportFormat.WEBP -> Bitmap.CompressFormat.WEBP
            }
            
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "edited_${System.currentTimeMillis()}.$extension")
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return false

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(compressFormat, quality, outputStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}




