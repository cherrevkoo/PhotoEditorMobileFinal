package com.practicum.photoeditormobile.tests

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.practicum.photoeditormobile.domain.FilterProvider
import com.practicum.photoeditormobile.domain.ImageProcessor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ImageProcessorTest {

    private lateinit var context: Context
    private lateinit var imageProcessor: ImageProcessor

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        imageProcessor = ImageProcessor(context)
    }

    @Test
    fun testApplyFilters() = runTest {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val result = imageProcessor.applyFilters(
            bitmap,
            FilterProvider.filters["Сепия"]!!.invoke(),
            null, 0f, 1f, 1f, 0f, 0f
        )
        assertNotNull(result)
    }
}