package com.practicum.photoeditormobile.tests

import com.practicum.photoeditormobile.data.CropRect
import org.junit.Assert.*
import org.junit.Test

class CropRectTest {

    @Test
    fun testCropRectValues() {
        val rect = CropRect(0f,0f,1f,1f)
        assertEquals(0f, rect.left)
        assertEquals(1f, rect.bottom)
    }
}