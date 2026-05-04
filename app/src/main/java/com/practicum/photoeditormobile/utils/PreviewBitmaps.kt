package com.practicum.photoeditormobile.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

object PreviewBitmaps {
    fun sample(width: Int = 1400, height: Int = 900): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF111827.toInt() }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bg)

        val paint1 = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF6366F1.toInt() }
        val paint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFEC4899.toInt() }
        val paint3 = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF22C55E.toInt() }

        canvas.drawRoundRect(RectF(80f, 80f, width * 0.55f, height * 0.6f), 48f, 48f, paint1)
        canvas.drawCircle(width * 0.78f, height * 0.35f, minOf(width, height) * 0.14f, paint2)
        canvas.drawRoundRect(RectF(width * 0.55f, height * 0.62f, width * 0.92f, height * 0.88f), 64f, 64f, paint3)

        val grid = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33FFFFFF
            strokeWidth = 2f
        }
        val step = (minOf(width, height) / 12).coerceAtLeast(40)
        var x = 0
        while (x <= width) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), grid)
            x += step
        }
        var y = 0
        while (y <= height) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), grid)
            y += step
        }

        return bitmap
    }
}