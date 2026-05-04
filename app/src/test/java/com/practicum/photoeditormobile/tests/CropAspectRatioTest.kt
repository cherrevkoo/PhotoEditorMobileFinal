package com.practicum.photoeditormobile.tests

import com.practicum.photoeditormobile.data.CropAspectRatio
import org.junit.Assert.assertEquals
import org.junit.Test

class CropAspectRatioTest {

    @Test
    fun testEnum() {
        assertEquals(CropAspectRatio.FREE, CropAspectRatio.valueOf("FREE"))
    }
}