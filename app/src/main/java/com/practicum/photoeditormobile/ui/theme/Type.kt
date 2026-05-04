package com.practicum.photoeditormobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.practicum.photoeditormobile.R

private val YsFontFamily = FontFamily(
    Font(R.font.ys_medium, weight = FontWeight.Medium),
    Font(R.font.ys_bold, weight = FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = YsFontFamily, fontWeight = FontWeight.Bold),
    displayMedium = TextStyle(fontFamily = YsFontFamily, fontWeight = FontWeight.Bold),
    displaySmall = TextStyle(fontFamily = YsFontFamily, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(
        fontFamily = YsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 46.sp
    ),
    headlineMedium = TextStyle(fontFamily = YsFontFamily, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontFamily = YsFontFamily, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontFamily = YsFontFamily, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontFamily = YsFontFamily, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontFamily = YsFontFamily, fontWeight = FontWeight.Medium),
    labelLarge = TextStyle(fontFamily = YsFontFamily, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontFamily = YsFontFamily, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontFamily = YsFontFamily, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(
        fontFamily = YsFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(fontFamily = YsFontFamily, fontWeight = FontWeight.Medium),
    bodySmall = TextStyle(fontFamily = YsFontFamily, fontWeight = FontWeight.Medium)
)