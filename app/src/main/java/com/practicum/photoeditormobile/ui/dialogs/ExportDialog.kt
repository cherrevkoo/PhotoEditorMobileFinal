package com.practicum.photoeditormobile.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.practicum.photoeditormobile.data.ExportFormat

@Composable
fun ExportDialog(
    format: ExportFormat,
    quality: Int,
    onFormatChange: (ExportFormat) -> Unit,
    onQualityChange: (Int) -> Unit,
    onExport: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки экспорта") },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Формат:", fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportFormat.values().forEach { fmt ->
                        FilterChip(
                            selected = format == fmt,
                            onClick = { onFormatChange(fmt) },
                            label = { Text(fmt.name) }
                        )
                    }
                }
                
                Text("Качество: $quality%", fontWeight = FontWeight.Bold)
                Slider(
                    value = quality.toFloat(),
                    onValueChange = { onQualityChange(it.toInt()) },
                    valueRange = 50f..100f,
                    steps = 9
                )
            }
        },
        confirmButton = {
            Button(onClick = onExport) {
                Text("Экспортировать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}




