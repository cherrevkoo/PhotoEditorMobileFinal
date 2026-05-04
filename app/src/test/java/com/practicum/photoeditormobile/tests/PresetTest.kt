package com.practicum.photoeditormobile.tests

import com.practicum.photoeditormobile.data.Preset
import org.junit.Assert.assertEquals
import org.junit.Test

class PresetTest {

    @Test
    fun testPresetCreation() {
        val preset = Preset("TestPreset","Sepia", 10f,1f,1f,0f,0f)
        assertEquals("TestPreset", preset.name)
    }
}