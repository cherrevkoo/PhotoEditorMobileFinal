package com.practicum.photoeditormobile.ui.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun AdjustSheet(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    warmth: Float,
    sharpness: Float,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onWarmthChange: (Float) -> Unit,
    onSharpnessChange: (Float) -> Unit,
    onApplyFinished: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Настройки",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        AdjustSlider(
            label = "Яркость",
            displayValue = brightness.roundToInt().coerceIn(0, 100).toString(),
            value = brightness.coerceIn(0f, 100f),
            valueRange = 0f..100f,
            icon = Icons.Default.Brightness4,
            onValueChange = { onBrightnessChange(it.coerceIn(0f, 100f)) },
            onValueChangeFinished = onApplyFinished
        )
        AdjustSlider(
            label = "Контраст",
            displayValue = String.format("%.2f", contrast.coerceIn(0f, 2f)),
            value = contrast.coerceIn(0f, 2f),
            valueRange = 0f..2f,
            icon = Icons.Default.Tune,
            onValueChange = { onContrastChange(it.coerceIn(0f, 2f)) },
            onValueChangeFinished = onApplyFinished
        )
        AdjustSlider(
            label = "Насыщенность",
            displayValue = String.format("%.2f", saturation.coerceIn(0f, 2f)),
            value = saturation.coerceIn(0f, 2f),
            valueRange = 0f..2f,
            icon = Icons.Default.Palette,
            onValueChange = { onSaturationChange(it.coerceIn(0f, 2f)) },
            onValueChangeFinished = onApplyFinished
        )
        AdjustSlider(
            label = "Теплота",
            displayValue = warmth.roundToInt().coerceIn(-50, 50).toString(),
            value = warmth.coerceIn(-50f, 50f),
            valueRange = -50f..50f,
            icon = Icons.Default.WbSunny,
            onValueChange = { onWarmthChange(it.coerceIn(-50f, 50f)) },
            onValueChangeFinished = onApplyFinished
        )
        AdjustSlider(
            label = "Резкость",
            displayValue = String.format("%.1f", sharpness.coerceIn(0f, 10f)),
            value = sharpness.coerceIn(0f, 10f),
            valueRange = 0f..10f,
            icon = Icons.Default.AutoFixHigh,
            onValueChange = { onSharpnessChange(it.coerceIn(0f, 10f)) },
            onValueChangeFinished = onApplyFinished
        )
        
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun AdjustSlider(
    label: String,
    displayValue: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                displayValue,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = { onValueChange(it.coerceIn(valueRange.start, valueRange.endInclusive)) },
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

