package com.practicum.photoeditormobile.domain

import com.practicum.photoeditormobile.data.Curve3
import kotlin.math.roundToInt

object CurvesLut {
    fun buildLut(curve: Curve3): IntArray {
        val y0 = curve.shadows.coerceIn(0, 255)
        val y1 = curve.midtones.coerceIn(0, 255)
        val y2 = curve.highlights.coerceIn(0, 255)

        val out = IntArray(256)
        for (x in 0..255) {
            val y = when {
                x <= 128 -> {
                    val t = x / 128f
                    (y0 + (y1 - y0) * t).roundToInt()
                }
                else -> {
                    val t = (x - 128) / 127f
                    (y1 + (y2 - y1) * t).roundToInt()
                }
            }
            out[x] = y.coerceIn(0, 255)
        }
        return out
    }

    fun applyLutPixel(argb: Int, masterLut: IntArray, rLut: IntArray, gLut: IntArray, bLut: IntArray): Int {
        val a = (argb ushr 24) and 0xFF
        var r = (argb ushr 16) and 0xFF
        var g = (argb ushr 8) and 0xFF
        var b = argb and 0xFF

        r = masterLut[r]
        g = masterLut[g]
        b = masterLut[b]

        r = rLut[r]
        g = gLut[g]
        b = bLut[b]

        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}

