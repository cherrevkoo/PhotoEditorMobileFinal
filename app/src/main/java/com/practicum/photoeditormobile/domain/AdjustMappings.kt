package com.practicum.photoeditormobile.domain

object AdjustMappings {
    fun brightnessUiToGpu(brightness0to100: Float): Float =
        ((brightness0to100.coerceIn(0f, 100f) - 50f) / 50f).coerceIn(-1f, 1f)

    fun contrastSafe(contrast0to2: Float): Float =
        contrast0to2.coerceIn(0f, 4f)

    fun saturationSafe(saturation0to2: Float): Float =
        saturation0to2.coerceIn(0f, 2f)

    fun warmthSafe(warmthMinus50to50: Float): Float =
        warmthMinus50to50.coerceIn(-50f, 50f)

    fun sharpnessToGpu(sharpness0to10: Float): Float =
        (sharpness0to10.coerceIn(0f, 10f) / 10f) * 4f
}

