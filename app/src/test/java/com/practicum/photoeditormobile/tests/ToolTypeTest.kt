package com.practicum.photoeditormobile.tests

import com.practicum.photoeditormobile.data.ToolType
import org.junit.Assert.assertEquals
import org.junit.Test

class ToolTypeTest {

    @Test
    fun testEnum() {
        assertEquals(ToolType.NONE, ToolType.valueOf("NONE"))
    }
}