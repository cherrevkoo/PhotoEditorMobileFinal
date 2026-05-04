package com.practicum.photoeditormobile.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.graphics.Bitmap
import com.practicum.photoeditormobile.data.CropAspectRatio
import com.practicum.photoeditormobile.data.CropRect
import com.practicum.photoeditormobile.utils.PreviewBitmaps
import com.practicum.photoeditormobile.ui.theme.PhotoEditorMobileTheme
import com.practicum.photoeditormobile.utils.AppLog
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CropScreen(
    bitmap: Bitmap?,
    onCropComplete: (CropRect) -> Unit,
    onCancel: () -> Unit,
    aspectRatio: CropAspectRatio = CropAspectRatio.FREE
) {
    if (bitmap == null) return
    var cropStart by remember { mutableStateOf<Offset?>(null) }
    var cropEnd by remember { mutableStateOf<Offset?>(null) }
    var currentRatio by remember { mutableStateOf(aspectRatio) }

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    val log = remember { "CropUI" }

    val targetRatio = when (currentRatio) {
        CropAspectRatio.FREE -> null
        CropAspectRatio.RATIO_1_1 -> 1f
        CropAspectRatio.RATIO_4_3 -> 4f / 3f
        CropAspectRatio.RATIO_3_4 -> 3f / 4f
        CropAspectRatio.RATIO_16_9 -> 16f / 9f
        CropAspectRatio.RATIO_9_16 -> 9f / 16f
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val imageWidth = bitmap.width.toFloat()
        val imageHeight = bitmap.height.toFloat()

        val scaleX = screenWidth / imageWidth
        val scaleY = screenHeight / imageHeight
        val scale = min(scaleX, scaleY)

        val displayedImageWidth = imageWidth * scale
        val displayedImageHeight = imageHeight * scale

        val offsetX = (screenWidth - displayedImageWidth) / 2f
        val offsetY = (screenHeight - displayedImageHeight) / 2f

        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    var accumulatedDrag = Offset.Zero
                    detectDragGestures(
                        onDragStart = { offset ->
                            val clamped = Offset(
                                x = offset.x.coerceIn(offsetX, offsetX + displayedImageWidth),
                                y = offset.y.coerceIn(offsetY, offsetY + displayedImageHeight)
                            )
                            cropStart = clamped
                            cropEnd = clamped
                            accumulatedDrag = Offset.Zero
                            AppLog.d(log, "dragStart=${clamped.x},${clamped.y} disp=${displayedImageWidth}x${displayedImageHeight}")
                        },
                        onDrag = { _, dragAmount ->
                            cropStart?.let { start ->
                                accumulatedDrag += dragAmount
                                val newEnd = start + accumulatedDrag
                                cropEnd = Offset(
                                    x = newEnd.x.coerceIn(offsetX, offsetX + displayedImageWidth),
                                    y = newEnd.y.coerceIn(offsetY, offsetY + displayedImageHeight)
                                )
                            }
                        },
                        onDragEnd = { accumulatedDrag = Offset.Zero }
                    )
                },
            contentScale = ContentScale.Fit
        )

        cropStart?.let { start ->
            cropEnd?.let { end ->
                var rectWidth = abs(end.x - start.x)
                var rectHeight = abs(end.y - start.y)
                var rectLeft = min(start.x, end.x)
                var rectTop = min(start.y, end.y)

                targetRatio?.let { ratio ->
                    if (rectHeight > 0 && rectWidth > 0) {
                        val currentRatio = rectWidth / rectHeight
                        if (currentRatio > ratio) {
                            rectWidth = rectHeight * ratio
                        } else {
                            rectHeight = rectWidth / ratio
                        }
                        val centerX = (start.x + end.x) / 2f
                        val centerY = (start.y + end.y) / 2f
                        rectLeft = centerX - rectWidth / 2f
                        rectTop = centerY - rectHeight / 2f
                    }
                }

                val rect = Rect(
                    offset = Offset(rectLeft, rectTop),
                    size = Size(rectWidth, rectHeight)
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val overlayPath = Path().apply {
                        addRect(Rect(Offset.Zero, size))
                        addRect(rect)
                        fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                    }

                    drawPath(overlayPath, color = Color.Black.copy(alpha = 0.7f))
                    drawRect(
                        color = Color.White,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Отмена")
            }
            Button(
                onClick = {
                    cropStart?.let { start ->
                        cropEnd?.let { end ->
                            AppLog.i(log, "applyCrop start=$start end=$end ratio=$currentRatio")
                            if (displayedImageWidth <= 0f || displayedImageHeight <= 0f) return@let

                            val clampedStartX = start.x.coerceIn(offsetX, offsetX + displayedImageWidth)
                            val clampedStartY = start.y.coerceIn(offsetY, offsetY + displayedImageHeight)
                            val clampedEndX = end.x.coerceIn(offsetX, offsetX + displayedImageWidth)
                            val clampedEndY = end.y.coerceIn(offsetY, offsetY + displayedImageHeight)

                            val nx1 = ((clampedStartX - offsetX) / displayedImageWidth)
                            val ny1 = ((clampedStartY - offsetY) / displayedImageHeight)
                            val nx2 = ((clampedEndX - offsetX) / displayedImageWidth)
                            val ny2 = ((clampedEndY - offsetY) / displayedImageHeight)

                            if (!nx1.isFinite() || !ny1.isFinite() || !nx2.isFinite() || !ny2.isFinite()) return@let

                            val normalizedStartX = min(nx1, nx2).coerceIn(0f, 1f)
                            val normalizedStartY = min(ny1, ny2).coerceIn(0f, 1f)
                            val normalizedEndX = max(nx1, nx2).coerceIn(0f, 1f)
                            val normalizedEndY = max(ny1, ny2).coerceIn(0f, 1f)

                            if (normalizedEndX - normalizedStartX > 0.01f && normalizedEndY - normalizedStartY > 0.01f) {
                                AppLog.i(log, "applyCrop rect=[$normalizedStartX,$normalizedStartY]-[$normalizedEndX,$normalizedEndY]")
                                onCropComplete(
                                    com.practicum.photoeditormobile.data.CropRect(
                                        normalizedStartX,
                                        normalizedStartY,
                                        normalizedEndX,
                                        normalizedEndY
                                    )
                                )
                            }
                        }
                    }
                },
                enabled = cropStart != null && cropEnd != null
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Применить")
            }
        }
    }
}

@Preview(showBackground = true, name = "Crop - Free", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun CropScreenPreviewLight() {
    PhotoEditorMobileTheme(darkTheme = false, dynamicColor = false) {
        CropScreen(
            bitmap = PreviewBitmaps.sample(),
            onCropComplete = {},
            onCancel = {},
            aspectRatio = CropAspectRatio.FREE
        )
    }
}

@Preview(showBackground = true, name = "Crop - Free (Dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CropScreenPreviewDark() {
    PhotoEditorMobileTheme(darkTheme = true, dynamicColor = false) {
        CropScreen(
            bitmap = PreviewBitmaps.sample(),
            onCropComplete = {},
            onCancel = {},
            aspectRatio = CropAspectRatio.FREE
        )
    }
}
