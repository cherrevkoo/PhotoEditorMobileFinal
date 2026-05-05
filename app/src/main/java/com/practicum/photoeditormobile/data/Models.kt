package com.practicum.photoeditormobile.data

import android.graphics.RectF

data class Curve3(
    val shadows: Int = 0,
    val midtones: Int = 128,
    val highlights: Int = 255
) {
    fun isIdentity(): Boolean = shadows == 0 && midtones == 128 && highlights == 255
}

data class EditState(
    val filterName: String,
    val effectName: String? = null,
    val autoEnhance: Boolean = false,
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val warmth: Float,
    val sharpness: Float,
    val curveMaster: Curve3 = Curve3(),
    val curveR: Curve3 = Curve3(),
    val curveG: Curve3 = Curve3(),
    val curveB: Curve3 = Curve3(),
    val rotation: Float,
    val cropRect: RectF?,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val textOverlay: String? = null,
    val textSizeScale: Float = 0.08f,
    val textBold: Boolean = false,
    val textFont: TextFont = TextFont.SANS
)

enum class TextFont {
    SANS, SERIF, MONO
}

data class CropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

data class Preset(
    val name: String,
    val filterName: String,
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val warmth: Float,
    val sharpness: Float
)

enum class ExportFormat {
    JPEG, PNG, WEBP
}

enum class CropAspectRatio {
    FREE, RATIO_1_1, RATIO_4_3, RATIO_3_4, RATIO_16_9, RATIO_9_16
}

enum class ToolType {
    FILTERS, ADJUST, CURVES, CROP, ROTATE, PRESETS, EFFECTS, NONE
}



