package com.practicum.photoeditormobile

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.practicum.photoeditormobile.data.Curve3
import com.practicum.photoeditormobile.domain.FilterProvider
import com.practicum.photoeditormobile.domain.ImageProcessor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GpuImageSmokeTest {

    @Test
    fun applySepiaAndVignette_notBlackAndSameSize() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val processor = ImageProcessor(context)

        val bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply {
            // colorful test pattern
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val r = (x * 255 / (width - 1)).coerceIn(0, 255)
                    val g = (y * 255 / (height - 1)).coerceIn(0, 255)
                    val b = ((x + y) * 255 / (width + height - 2)).coerceIn(0, 255)
                    setPixel(x, y, (0xFF shl 24) or (r shl 16) or (g shl 8) or b)
                }
            }
        }

        val base = FilterProvider.filters.getValue("Сепия").invoke()
        val effect = FilterProvider.effects.getValue("Виньетка").invoke()

        val out = processor.applyFilters(
            bitmap = bmp,
            baseFilter = base,
            effectFilter = effect,
            brightness = 0f,
            contrast = 1f,
            saturation = 1f,
            sharpness = 0f,
            warmth = 0f
        )

        assertNotNull(out)
        out!!
        assertEquals(bmp.width, out.width)
        assertEquals(bmp.height, out.height)

        val pixels = IntArray(out.width * out.height)
        out.getPixels(pixels, 0, out.width, 0, 0, out.width, out.height)

        // "not all black": check a few samples and sum of RGB
        val sampleIndices = listOf(0, pixels.size / 3, (pixels.size * 2) / 3, pixels.size - 1)
        val rgbSum = sampleIndices.sumOf { idx ->
            val p = pixels[idx]
            ((p ushr 16) and 0xFF) + ((p ushr 8) and 0xFF) + (p and 0xFF)
        }
        assertTrue("Output looks fully black", rgbSum > 0)
    }

    @Test
    fun applyToneCurves_notBlackAndSameSize() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val processor = ImageProcessor(context)

        val bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val r = (x * 255 / (width - 1)).coerceIn(0, 255)
                    val g = (y * 255 / (height - 1)).coerceIn(0, 255)
                    val b = ((x + y) * 255 / (width + height - 2)).coerceIn(0, 255)
                    setPixel(x, y, (0xFF shl 24) or (r shl 16) or (g shl 8) or b)
                }
            }
        }

        val out = processor.applyFilters(
            bitmap = bmp,
            baseFilter = GPUImageFilter(),
            effectFilter = null,
            brightness = 0f,
            contrast = 1f,
            saturation = 1f,
            sharpness = 0f,
            warmth = 0f,
            curveMaster = Curve3(shadows = 0, midtones = 150, highlights = 255),
            curveR = Curve3(shadows = 0, midtones = 120, highlights = 255),
            curveG = Curve3(),
            curveB = Curve3()
        )

        assertNotNull(out)
        out!!
        assertEquals(bmp.width, out.width)
        assertEquals(bmp.height, out.height)

        val pixels = IntArray(out.width * out.height)
        out.getPixels(pixels, 0, out.width, 0, 0, out.width, out.height)
        val sampleIndices = listOf(0, pixels.size / 3, (pixels.size * 2) / 3, pixels.size - 1)
        val rgbSum = sampleIndices.sumOf { idx ->
            val p = pixels[idx]
            ((p ushr 16) and 0xFF) + ((p ushr 8) and 0xFF) + (p and 0xFF)
        }
        assertTrue("Curves output looks fully black", rgbSum > 0)
    }
}

