package com.practicum.photoeditormobile.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import com.practicum.photoeditormobile.data.CropRect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BitmapUtils {
    private const val LOG = "Bitmap"
    private fun openInputStream(context: Context, uri: Uri) =
        when (uri.scheme) {
            "android.resource" -> {
                val pkg = uri.authority ?: context.packageName
                val res = if (pkg == context.packageName) context.resources else {
                    context.packageManager.getResourcesForApplication(pkg)
                }

                val segments = uri.pathSegments
                val resId = when {
                    segments.size >= 2 -> {
                        val type = segments[0]
                        val name = segments[1]
                        res.getIdentifier(name, type, pkg)
                    }
                    segments.size == 1 -> segments[0].toIntOrNull() ?: 0
                    else -> 0
                }

                if (resId != 0) {
                    AppLog.d(LOG, "openInputStream(android.resource): pkg=$pkg resId=$resId uri=$uri")
                    res.openRawResource(resId)
                } else {
                    AppLog.w(LOG, "openInputStream(android.resource): resId not found for uri=$uri")
                    null
                }
            }
            else -> {
                AppLog.d(LOG, "openInputStream: scheme=${uri.scheme} uri=$uri")
                context.contentResolver.openInputStream(uri)
            }
        }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    suspend fun decodeSampledBitmapFromUriAsync(
        context: Context,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int,
        inBitmap: Bitmap? = null
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            AppLog.i(LOG, "decodeAsync(uri=$uri req=${reqWidth}x$reqHeight inBitmap=${inBitmap?.width}x${inBitmap?.height})")
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openInputStream(context, uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            if (inBitmap != null && !inBitmap.isRecycled) options.inBitmap = inBitmap
            val decoded = openInputStream(context, uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            AppLog.i(LOG, "decodeAsync result=${decoded?.width}x${decoded?.height} inSample=${options.inSampleSize}")
            decoded
        } catch (e: Exception) {
            AppLog.e(LOG, "decodeAsync exception: ${e.message}", e)
            null
        }
    }

    fun decodeSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int,
        inBitmap: Bitmap? = null
    ): Bitmap? {
        return runCatching {
            AppLog.i(LOG, "decode(uri=$uri req=${reqWidth}x$reqHeight inBitmap=${inBitmap?.width}x${inBitmap?.height})")
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openInputStream(context, uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            if (inBitmap != null && !inBitmap.isRecycled) options.inBitmap = inBitmap
            val decoded = openInputStream(context, uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            AppLog.i(LOG, "decode result=${decoded?.width}x${decoded?.height} inSample=${options.inSampleSize}")
            decoded
        }.getOrNull()
    }
    fun flipBitmap(bitmap: Bitmap?, horizontal: Boolean, vertical: Boolean): Bitmap? {
        if (bitmap == null) return null
        if (!horizontal && !vertical) return bitmap
        return try {
            val matrix = Matrix().apply {
                preScale(if (horizontal) -1f else 1f, if (vertical) -1f else 1f)
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: OutOfMemoryError) {
            AppLog.e(LOG, "flipBitmap OOM (${bitmap.width}x${bitmap.height})", e)
            bitmap
        } catch (e: Exception) {
            AppLog.e(LOG, "flipBitmap exception: ${e.message}", e)
            bitmap
        }
    }

    fun rotateBitmap(bitmap: Bitmap?, degrees: Float): Bitmap? {
        if (bitmap == null || degrees % 360f == 0f) return bitmap
        return try {
            val matrix = Matrix().apply { postRotate(degrees) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: OutOfMemoryError) {
            AppLog.e(LOG, "rotateBitmap OOM (${bitmap.width}x${bitmap.height}) deg=$degrees", e)
            bitmap
        } catch (e: Exception) {
            AppLog.e(LOG, "rotateBitmap exception: ${e.message} deg=$degrees", e)
            bitmap
        }
    }

    fun cropBitmap(bitmap: Bitmap?, cropRect: CropRect): Bitmap? {
        if (bitmap == null) return null
        return try {
            val bw = bitmap.width
            val bh = bitmap.height

            val leftPx = (cropRect.left * bw).toInt().coerceIn(0, bw - 1)
            val topPx = (cropRect.top * bh).toInt().coerceIn(0, bh - 1)
            val rightPx = (cropRect.right * bw).toInt().coerceIn(leftPx + 1, bw)
            val bottomPx = (cropRect.bottom * bh).toInt().coerceIn(topPx + 1, bh)

            val width = (rightPx - leftPx).coerceAtLeast(1)
            val height = (bottomPx - topPx).coerceAtLeast(1)

            Bitmap.createBitmap(bitmap, leftPx, topPx, width, height)
        } catch (e: IllegalArgumentException) {
            AppLog.e(LOG, "cropBitmap illegalArg crop=$cropRect bmp=${bitmap.width}x${bitmap.height}", e)
            bitmap
        } catch (e: OutOfMemoryError) {
            AppLog.e(LOG, "cropBitmap OOM crop=$cropRect bmp=${bitmap.width}x${bitmap.height}", e)
            bitmap
        } catch (e: Exception) {
            AppLog.e(LOG, "cropBitmap exception: ${e.message} crop=$cropRect", e)
            bitmap
        }
    }
}