package com.practicum.photoeditormobile.tests

import com.practicum.photoeditormobile.domain.AdjustMappings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdjustMappingsTest {

    @Test
    fun brightnessUiToGpu_mapsEndpoints() {
        assertEquals(-1f, AdjustMappings.brightnessUiToGpu(0f), 1e-6f)
        assertEquals(0f, AdjustMappings.brightnessUiToGpu(50f), 1e-6f)
        assertEquals(1f, AdjustMappings.brightnessUiToGpu(100f), 1e-6f)
    }

    @Test
    fun sharpnessToGpu_mapsRange() {
        assertEquals(0f, AdjustMappings.sharpnessToGpu(0f), 1e-6f)
        assertEquals(4f, AdjustMappings.sharpnessToGpu(10f), 1e-6f)
    }

    @Test
    fun warmthSafe_clamps() {
        assertEquals(-50f, AdjustMappings.warmthSafe(-999f), 1e-6f)
        assertEquals(50f, AdjustMappings.warmthSafe(999f), 1e-6f)
    }

    @Test
    fun contrastSafe_clamps() {
        assertEquals(0f, AdjustMappings.contrastSafe(-2f), 1e-6f)
        assertEquals(4f, AdjustMappings.contrastSafe(999f), 1e-6f)
    }

    @Test
    fun saturationSafe_clamps() {
        assertEquals(0f, AdjustMappings.saturationSafe(-2f), 1e-6f)
        assertEquals(2f, AdjustMappings.saturationSafe(999f), 1e-6f)
    }
}

