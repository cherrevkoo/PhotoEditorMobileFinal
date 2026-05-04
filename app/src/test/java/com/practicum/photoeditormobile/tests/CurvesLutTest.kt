package com.practicum.photoeditormobile.tests

import com.practicum.photoeditormobile.data.Curve3
import com.practicum.photoeditormobile.domain.CurvesLut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurvesLutTest {

    @Test
    fun identityCurve_producesIdentityLut() {
        val lut = CurvesLut.buildLut(Curve3())
        for (i in 0..255) {
            assertEquals(i, lut[i])
        }
    }

    @Test
    fun monotonicControlPoints_produceNonDecreasingLut() {
        val lut = CurvesLut.buildLut(Curve3(shadows = 10, midtones = 120, highlights = 240))
        var prev = -1
        for (i in 0..255) {
            assertTrue(lut[i] >= prev)
            prev = lut[i]
        }
    }

    @Test
    fun lut_valuesStayInRange() {
        val lut = CurvesLut.buildLut(Curve3(shadows = -50, midtones = 999, highlights = 260))
        for (i in 0..255) {
            assertTrue(lut[i] in 0..255)
        }
    }

    @Test
    fun applyLutPixel_identityLeavesPixelSame() {
        val id = CurvesLut.buildLut(Curve3())
        val px = 0xFF3366CC.toInt()
        val out = CurvesLut.applyLutPixel(px, id, id, id, id)
        assertEquals(px, out)
    }

    @Test
    fun applyLutPixel_matchesNestedLookupForMasterPlusRgb() {
        val master = CurvesLut.buildLut(Curve3(shadows = 5, midtones = 130, highlights = 250))
        val rLut = CurvesLut.buildLut(Curve3(shadows = 0, midtones = 140, highlights = 255))
        val gLut = CurvesLut.buildLut(Curve3())
        val bLut = CurvesLut.buildLut(Curve3(shadows = 0, midtones = 120, highlights = 255))

        val px = 0xFF2040AA.toInt()
        val expectedR = rLut[master[(px ushr 16) and 0xFF]]
        val expectedG = gLut[master[(px ushr 8) and 0xFF]]
        val expectedB = bLut[master[px and 0xFF]]
        val expected = (0xFF shl 24) or (expectedR shl 16) or (expectedG shl 8) or expectedB

        assertEquals(expected, CurvesLut.applyLutPixel(px, master, rLut, gLut, bLut))
    }
}

