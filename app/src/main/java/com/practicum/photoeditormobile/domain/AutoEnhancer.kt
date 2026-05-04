package com.practicum.photoeditormobile.domain

import android.graphics.Bitmap
import kotlin.math.roundToInt

object AutoEnhancer {
    data class Params(
        val clipPercentPerTail: Float = 0.5f,
        val enableAutoColorBalance: Boolean = true,
        val enableAutoContrast: Boolean = true,
        val enableAutoLevels: Boolean = true
    )

    fun autoEnhance(bitmap: Bitmap, params: Params = Params()): Bitmap {
        val src = ensureArgb(bitmap)
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        var working = pixels

        if (params.enableAutoColorBalance) {
            working = applyGrayWorld(working)
        }
        if (params.enableAutoContrast) {
            working = applyAutoContrastLuma(working, params.clipPercentPerTail)
        }
        if (params.enableAutoLevels) {
            working = applyAutoLevelsPerChannel(working, params.clipPercentPerTail)
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(working, 0, w, 0, 0, w, h)
        return out
    }

    private fun ensureArgb(bitmap: Bitmap): Bitmap {
        return if (bitmap.config == Bitmap.Config.ARGB_8888) bitmap else bitmap.copy(Bitmap.Config.ARGB_8888, false)
    }

    private fun applyGrayWorld(pixels: IntArray): IntArray {
        var sumR = 0L
        var sumG = 0L
        var sumB = 0L
        val n = pixels.size.coerceAtLeast(1)

        for (px in pixels) {
            sumR += (px shr 16) and 0xFF
            sumG += (px shr 8) and 0xFF
            sumB += (px) and 0xFF
        }

        val meanR = sumR.toDouble() / n
        val meanG = sumG.toDouble() / n
        val meanB = sumB.toDouble() / n

        val target = (meanR + meanG + meanB) / 3.0
        val scaleR = if (meanR > 1e-6) target / meanR else 1.0
        val scaleG = if (meanG > 1e-6) target / meanG else 1.0
        val scaleB = if (meanB > 1e-6) target / meanB else 1.0

        val out = IntArray(pixels.size)
        for (i in pixels.indices) {
            val px = pixels[i]
            val a = (px ushr 24) and 0xFF
            val r = (px ushr 16) and 0xFF
            val g = (px ushr 8) and 0xFF
            val b = (px) and 0xFF

            val nr = clamp255((r * scaleR).roundToInt())
            val ng = clamp255((g * scaleG).roundToInt())
            val nb = clamp255((b * scaleB).roundToInt())
            out[i] = (a shl 24) or (nr shl 16) or (ng shl 8) or nb
        }
        return out
    }

    private fun applyAutoContrastLuma(pixels: IntArray, clipPercentPerTail: Float): IntArray {
        val hist = IntArray(256)
        for (px in pixels) {
            val r = (px ushr 16) and 0xFF
            val g = (px ushr 8) and 0xFF
            val b = (px) and 0xFF
            val y = (0.2126 * r + 0.7152 * g + 0.0722 * b).roundToInt().coerceIn(0, 255)
            hist[y]++
        }
        val (low, high) = findLowHigh(hist, pixels.size, clipPercentPerTail)
        if (high <= low) return pixels

        val scale = 255.0 / (high - low).toDouble()
        val offset = -low.toDouble() * scale

        val out = IntArray(pixels.size)
        for (i in pixels.indices) {
            val px = pixels[i]
            val a = (px ushr 24) and 0xFF
            val r = (px ushr 16) and 0xFF
            val g = (px ushr 8) and 0xFF
            val b = (px) and 0xFF

            val nr = clamp255((r * scale + offset).roundToInt())
            val ng = clamp255((g * scale + offset).roundToInt())
            val nb = clamp255((b * scale + offset).roundToInt())
            out[i] = (a shl 24) or (nr shl 16) or (ng shl 8) or nb
        }
        return out
    }

    private fun applyAutoLevelsPerChannel(pixels: IntArray, clipPercentPerTail: Float): IntArray {
        val histR = IntArray(256)
        val histG = IntArray(256)
        val histB = IntArray(256)
        for (px in pixels) {
            histR[(px ushr 16) and 0xFF]++
            histG[(px ushr 8) and 0xFF]++
            histB[(px) and 0xFF]++
        }

        val (rLow, rHigh) = findLowHigh(histR, pixels.size, clipPercentPerTail)
        val (gLow, gHigh) = findLowHigh(histG, pixels.size, clipPercentPerTail)
        val (bLow, bHigh) = findLowHigh(histB, pixels.size, clipPercentPerTail)

        val rScale = if (rHigh > rLow) 255.0 / (rHigh - rLow) else 1.0
        val gScale = if (gHigh > gLow) 255.0 / (gHigh - gLow) else 1.0
        val bScale = if (bHigh > bLow) 255.0 / (bHigh - bLow) else 1.0

        val out = IntArray(pixels.size)
        for (i in pixels.indices) {
            val px = pixels[i]
            val a = (px ushr 24) and 0xFF
            val r = (px ushr 16) and 0xFF
            val g = (px ushr 8) and 0xFF
            val b = (px) and 0xFF

            val nr = clamp255(((r - rLow) * rScale).roundToInt())
            val ng = clamp255(((g - gLow) * gScale).roundToInt())
            val nb = clamp255(((b - bLow) * bScale).roundToInt())
            out[i] = (a shl 24) or (nr shl 16) or (ng shl 8) or nb
        }
        return out
    }

    private fun findLowHigh(hist: IntArray, total: Int, clipPercentPerTail: Float): Pair<Int, Int> {
        val clip = (total * (clipPercentPerTail.coerceAtLeast(0f) / 100f)).roundToInt()
        var cumulative = 0
        var low = 0
        while (low < 256) {
            cumulative += hist[low]
            if (cumulative > clip) break
            low++
        }

        cumulative = 0
        var high = 255
        while (high >= 0) {
            cumulative += hist[high]
            if (cumulative > clip) break
            high--
        }

        return low.coerceIn(0, 255) to high.coerceIn(0, 255)
    }

    private fun clamp255(v: Int): Int = when {
        v < 0 -> 0
        v > 255 -> 255
        else -> v
    }
}

