package com.practicum.photoeditormobile.tests

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.practicum.photoeditormobile.data.Curve3
import com.practicum.photoeditormobile.viewmodel.PhotoEditorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PhotoEditorViewModelCurvesTest {

    private lateinit var viewModel: PhotoEditorViewModel
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        viewModel = PhotoEditorViewModel()
        viewModel.initialize(context)
    }

    @Test
    fun curveChangeIsCapturedInHistory_andDoesNotResetOtherAdjustments() {
        viewModel.setImageForTest(Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888))
        viewModel.contrast = 1.25f
        viewModel.saveToHistoryIfChanged()

        val indexBefore = viewModel.historyIndex
        assertEquals(indexBefore, viewModel.history.lastIndex)

        val beforeContrast = viewModel.contrast
        viewModel.updateCurveMaster(Curve3(shadows = 10, midtones = 140, highlights = 245))
        viewModel.saveToHistoryIfChanged()

        assertEquals(beforeContrast, viewModel.contrast)
        assertEquals(viewModel.historyIndex, indexBefore + 1)
        val last = viewModel.history[viewModel.historyIndex]
        assertNotEquals(Curve3(), last.curveMaster)
    }
}
