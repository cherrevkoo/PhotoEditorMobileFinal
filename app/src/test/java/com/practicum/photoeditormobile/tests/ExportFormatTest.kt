package com.practicum.photoeditormobile.tests

import com.practicum.photoeditormobile.data.ExportFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportFormatTest {

    @Test
    fun testEnum() {
        assertEquals(ExportFormat.JPEG, ExportFormat.valueOf("JPEG"))
    }
}