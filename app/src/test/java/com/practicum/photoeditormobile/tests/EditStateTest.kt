package com.practicum.photoeditormobile.tests

import com.practicum.photoeditormobile.data.EditState
import org.junit.Assert.*
import org.junit.Test

class EditStateTest {

    @Test
    fun testEditStateInit() {
        val state = EditState(
            filterName = "Sepia",
            effectName = null,
            autoEnhance = false,
            brightness = 10f,
            contrast = 1f,
            saturation = 1f,
            warmth = 0f,
            sharpness = 0f,
            rotation = 0f,
            cropRect = null,
            flipHorizontal = false,
            flipVertical = false
        )
        assertEquals("Sepia", state.filterName)
    }
}