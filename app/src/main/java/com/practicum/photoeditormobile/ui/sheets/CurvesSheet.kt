package com.practicum.photoeditormobile.ui.sheets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.practicum.photoeditormobile.data.Curve3
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

private enum class CurveUiChannel {
    MASTER, R, G, B
}

@Composable
fun CurvesSheet(
    curveMaster: Curve3,
    curveR: Curve3,
    curveG: Curve3,
    curveB: Curve3,
    onCurveMasterChange: (Curve3) -> Unit,
    onCurveRChange: (Curve3) -> Unit,
    onCurveGChange: (Curve3) -> Unit,
    onCurveBChange: (Curve3) -> Unit,
    onApplyFinished: () -> Unit = {}
) {
    var channel by remember { mutableIntStateOf(CurveUiChannel.MASTER.ordinal) }

    val currentCurve: Curve3 = when (CurveUiChannel.entries[channel]) {
        CurveUiChannel.MASTER -> curveMaster
        CurveUiChannel.R -> curveR
        CurveUiChannel.G -> curveG
        CurveUiChannel.B -> curveB
    }
    val latestCurve by rememberUpdatedState(currentCurve)

    val onCurveChange: (Curve3) -> Unit = { c ->
        when (CurveUiChannel.entries[channel]) {
            CurveUiChannel.MASTER -> onCurveMasterChange(c)
            CurveUiChannel.R -> onCurveRChange(c)
            CurveUiChannel.G -> onCurveGChange(c)
            CurveUiChannel.B -> onCurveBChange(c)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Кривые",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = {
                    onCurveMasterChange(Curve3())
                    onCurveRChange(Curve3())
                    onCurveGChange(Curve3())
                    onCurveBChange(Curve3())
                    onApplyFinished()
                }
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Сбросить кривые")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = channel == CurveUiChannel.MASTER.ordinal,
                onClick = { channel = CurveUiChannel.MASTER.ordinal },
                label = { Text("RGB") }
            )
            FilterChip(
                selected = channel == CurveUiChannel.R.ordinal,
                onClick = { channel = CurveUiChannel.R.ordinal },
                label = { Text("R") }
            )
            FilterChip(
                selected = channel == CurveUiChannel.G.ordinal,
                onClick = { channel = CurveUiChannel.G.ordinal },
                label = { Text("G") }
            )
            FilterChip(
                selected = channel == CurveUiChannel.B.ordinal,
                onClick = { channel = CurveUiChannel.B.ordinal },
                label = { Text("B") }
            )
        }

        Text(
            "Тени · Средние · Света — перетаскивайте точки по вертикали",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val curveColor = when (CurveUiChannel.entries[channel]) {
            CurveUiChannel.MASTER -> MaterialTheme.colorScheme.primary
            CurveUiChannel.R -> Color(0xFFE57373)
            CurveUiChannel.G -> Color(0xFF81C784)
            CurveUiChannel.B -> Color(0xFF64B5F6)
        }
        val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        val handleBorderColor = MaterialTheme.colorScheme.surface

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .pointerInput(channel) {
                    val pad = 18.dp.toPx()
                    val w = size.width
                    val h = size.height
                    val plotW = max(1f, w - pad * 2)
                    val plotH = max(1f, h - pad * 2)
                    var activePointIndex = -1

                    fun yToOutput(yPx: Float): Int {
                        val t = ((yPx - pad) / plotH).coerceIn(0f, 1f)
                        return (255f * (1f - t)).roundToInt().coerceIn(0, 255)
                    }

                    fun pointPositions(curve: Curve3): Triple<Offset, Offset, Offset> {
                        val x0 = pad + 0f * plotW / 255f
                        val x1 = pad + 128f * plotW / 255f
                        val x2 = pad + 255f * plotW / 255f
                        fun yFor(v: Int): Float = pad + plotH * (1f - (v / 255f))
                        return Triple(
                            Offset(x0, yFor(curve.shadows)),
                            Offset(x1, yFor(curve.midtones)),
                            Offset(x2, yFor(curve.highlights))
                        )
                    }

                    fun nearestIndex(pos: Offset, curve: Curve3): Int {
                        val (p0, p1, p2) = pointPositions(curve)
                        val d0 = sqrt((pos.x - p0.x) * (pos.x - p0.x) + (pos.y - p0.y) * (pos.y - p0.y))
                        val d1 = sqrt((pos.x - p1.x) * (pos.x - p1.x) + (pos.y - p1.y) * (pos.y - p1.y))
                        val d2 = sqrt((pos.x - p2.x) * (pos.x - p2.x) + (pos.y - p2.y) * (pos.y - p2.y))
                        return when {
                            d0 <= d1 && d0 <= d2 -> 0
                            d1 <= d2 -> 1
                            else -> 2
                        }
                    }

                    detectDragGestures(
                        onDragStart = { start ->
                            activePointIndex = nearestIndex(start, latestCurve)
                        },
                        onDragCancel = {
                            activePointIndex = -1
                            onApplyFinished()
                        },
                        onDragEnd = {
                            activePointIndex = -1
                            onApplyFinished()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (activePointIndex !in 0..2) return@detectDragGestures
                            val newY = yToOutput(change.position.y + dragAmount.y)
                            val base = latestCurve
                            val updated = when (activePointIndex) {
                                0 -> base.copy(shadows = newY)
                                1 -> base.copy(midtones = newY)
                                else -> base.copy(highlights = newY)
                            }
                            onCurveChange(updated)
                        }
                    )
                }
        ) {
            val pad = 18.dp.toPx()
            val plotW = max(1f, size.width - pad * 2)
            val plotH = max(1f, size.height - pad * 2)

            // grid
            for (i in 1..3) {
                val x = pad + plotW * i / 4f
                drawLine(gridColor, Offset(x, pad), Offset(x, pad + plotH), strokeWidth = 1.dp.toPx())
                val y = pad + plotH * i / 4f
                drawLine(gridColor, Offset(pad, y), Offset(pad + plotW, y), strokeWidth = 1.dp.toPx())
            }

            // diagonal (identity)
            drawLine(
                axisColor,
                Offset(pad, pad + plotH),
                Offset(pad + plotW, pad),
                strokeWidth = 1.dp.toPx()
            )

            // frame
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(pad, pad),
                size = Size(plotW, plotH),
                style = Stroke(width = 1.dp.toPx())
            )

            fun yFor(v: Int): Float = pad + plotH * (1f - (v / 255f))
            val x0 = pad + 0f * plotW / 255f
            val x1 = pad + 128f * plotW / 255f
            val x2 = pad + 255f * plotW / 255f
            val p0 = Offset(x0, yFor(currentCurve.shadows))
            val p1 = Offset(x1, yFor(currentCurve.midtones))
            val p2 = Offset(x2, yFor(currentCurve.highlights))

            val path = Path().apply {
                moveTo(p0.x, p0.y)
                lineTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
            }
            drawPath(path, color = curveColor, style = Stroke(width = 3.dp.toPx()))

            val handleFill = curveColor.copy(alpha = 0.9f)
            listOf(p0, p1, p2).forEach { center ->
                drawCircle(handleBorderColor, radius = 10.dp.toPx(), center = center)
                drawCircle(handleFill, radius = 8.dp.toPx(), center = center)
            }
        }
    }
}
