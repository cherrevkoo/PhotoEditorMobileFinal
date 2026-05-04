package com.practicum.photoeditormobile.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BitmapUtilsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testCalculateInSampleSize_smallerThanRequested() {
        val options = BitmapFactory.Options().apply {
            outWidth = 4000
            outHeight = 3000
        }
        val sampleSize = BitmapUtils.calculateInSampleSize(options, 1000, 1000)
        assertTrue(sampleSize >= 1)
    }

    @Test
    fun testDecodeSampledBitmapFromUriAsync_returnsBitmap() = runBlocking {
        val uri: Uri = Uri.parse("android.resource://${context.packageName}/drawable/ic_launcher")
        val bitmap: Bitmap? = BitmapUtils.decodeSampledBitmapFromUriAsync(context, uri, 200, 200)
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width <= 200 && bitmap.height <= 200)
    }

    @Test
    fun testDecodeSampledBitmapFromUri_sync() {
        val uri: Uri = Uri.parse("android.resource://${context.packageName}/drawable/ic_launcher")
        val bitmap: Bitmap? = BitmapUtils.decodeSampledBitmapFromUri(context, uri, 100, 100)
        assertNotNull(bitmap)
        assertTrue(bitmap!!.width <= 100 && bitmap.height <= 100)
    }
}