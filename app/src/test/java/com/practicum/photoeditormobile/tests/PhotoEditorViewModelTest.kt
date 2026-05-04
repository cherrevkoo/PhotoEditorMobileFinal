package com.practicum.photoeditormobile.tests

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.practicum.photoeditormobile.viewmodel.PhotoEditorViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PhotoEditorViewModelTest {

    private lateinit var viewModel: PhotoEditorViewModel
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        viewModel = PhotoEditorViewModel()
        viewModel.initialize(context)
    }

    @Test
    fun testLoadImage() = runTest {
        viewModel.setImageForTest(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888))
        val bitmap: Bitmap? = viewModel.currentBitmap
        assertNotNull(bitmap)
    }

    @Test
    fun testApplyAllFilters() = runTest {
        viewModel.setImageForTest(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888))
        viewModel.currentFilterName = "Сепия"
        viewModel.applyAll()
        advanceUntilIdle()
        val bitmap: Bitmap? = viewModel.currentBitmap
        assertNotNull(bitmap)
    }
}