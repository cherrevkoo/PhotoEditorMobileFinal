package com.practicum.photoeditormobile.ui.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.practicum.photoeditormobile.ui.components.FilterItem

@Composable
fun EffectsSheet(
    effects: List<String>,
    currentEffect: String?,
    onEffectSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Эффекты",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(effects) { name ->
                FilterItem(
                    name = name,
                    selected = name == currentEffect,
                    onClick = { onEffectSelected(name) }
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}




