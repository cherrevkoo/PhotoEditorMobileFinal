package com.practicum.photoeditormobile.ui.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RotateSheet(
    rotation: Float,
    onRotationChange: (Float) -> Unit,
    onRotate90: () -> Unit,
    onRotate90Clockwise: () -> Unit,
    flipHorizontal: Boolean,
    flipVertical: Boolean,
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Поворот",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onRotate90) {
                Icon(Icons.Default.RotateLeft, "Повернуть на 90° против часовой", modifier = Modifier.size(32.dp))
            }
            Text(
                "${rotation.toInt()}°",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onRotate90Clockwise) {
                Icon(Icons.Default.RotateRight, "Повернуть на 90° по часовой", modifier = Modifier.size(32.dp))
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            "Угол поворота",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Slider(
            value = rotation,
            onValueChange = onRotationChange,
            valueRange = 0f..359f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(24.dp))
        
        Divider()
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            "Отражение",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onFlipHorizontal,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (flipHorizontal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.Default.CompareArrows, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("По горизонтали")
            }
            Button(
                onClick = onFlipVertical,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (flipVertical) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.Default.CompareArrows, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("По вертикали")
            }
        }
        
        Spacer(Modifier.height(24.dp))
    }
}



