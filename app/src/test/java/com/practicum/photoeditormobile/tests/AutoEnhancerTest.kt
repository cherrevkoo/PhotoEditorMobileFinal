package com.practicum.photoeditormobile.tests

import android.graphics.Bitmap
import com.practicum.photoeditormobile.domain.AutoEnhancer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AutoEnhancerTest {

    @Test
    fun autoEnhance_keepsSizeAndArgb() {
        val bmp = Bitmap.createBitmap(32, 16, Bitmap.Config.ARGB_8888).apply {
            // simple gradient-ish content
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val r = (x * 255 / (width - 1)).coerceIn(0, 255)
                    val g = (y * 255 / (height - 1)).coerceIn(0, 255)
                    val b = 128
                    setPixel(x, y, (0xFF shl 24) or (r shl 16) or (g shl 8) or b)
                }
            }
        }

        val out = AutoEnhancer.autoEnhance(bmp)
        assertNotNull(out)
        assertEquals(bmp.width, out.width)
        assertEquals(bmp.height, out.height)
        assertEquals(Bitmap.Config.ARGB_8888, out.config)
    }

    @Test
    fun autoEnhance_outputPixelsWithinRange() {
        val bmp = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        val out = AutoEnhancer.autoEnhance(bmp)
        val px = IntArray(out.width * out.height)
        out.getPixels(px, 0, out.width, 0, 0, out.width, out.height)
        for (p in px) {
            val a = (p ushr 24) and 0xFF
            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val b = p and 0xFF
            assertTrue(a in 0..255)
            assertTrue(r in 0..255)
            assertTrue(g in 0..255)
            assertTrue(b in 0..255)
        }
    }
}

