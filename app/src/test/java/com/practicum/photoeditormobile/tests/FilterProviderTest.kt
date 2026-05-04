package com.practicum.photoeditormobile.domain

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FilterProviderTest {

    @Test
    fun testFiltersExist() {
        val filters = FilterProvider.filters
        assertTrue(filters.containsKey("Сепия"))
        assertTrue(filters.containsKey("Черно-белый"))
        // Factories should exist; instantiation of some GPU filters may require GL context on JVM.
    }

    @Test
    fun testEffectsExist() {
        val effects = FilterProvider.effects
        assertTrue(effects.containsKey("Виньетка"))
        // Factories should exist; instantiation is covered by androidTest smoke with real device/emu.
    }
}