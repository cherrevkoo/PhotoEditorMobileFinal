package com.practicum.photoeditormobile.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.practicum.photoeditormobile.data.*
import com.practicum.photoeditormobile.domain.FilterProvider
import com.practicum.photoeditormobile.ui.components.ToolButton
import com.practicum.photoeditormobile.ui.dialogs.ExportDialog
import com.practicum.photoeditormobile.utils.PreviewBitmaps
import com.practicum.photoeditormobile.ui.sheets.*
import com.practicum.photoeditormobile.ui.theme.PhotoEditorMobileTheme
import com.practicum.photoeditormobile.ui.theme.ToolPanelPurpleDark
import com.practicum.photoeditormobile.utils.AppLog
import com.practicum.photoeditormobile.viewmodel.PhotoEditorViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    viewModel: PhotoEditorViewModel,
    onImageSelected: (android.net.Uri) -> Unit,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {}
) {
    val log = remember { "UI" }
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val effectiveDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
        AppLog.i(log, "screen init")
    }

    LaunchedEffect(
        viewModel.rotation,
        viewModel.flipHorizontal,
        viewModel.flipVertical,
        viewModel.currentFilterName,
        viewModel.currentEffectName,
        viewModel.brightness,
        viewModel.contrast,
        viewModel.saturation,
        viewModel.sharpness,
        viewModel.warmth,
        viewModel.autoEnhanceEnabled,
        viewModel.curvesVersion
    ) {
        if (viewModel.originalBitmap != null && !viewModel.showCropMode) {
            viewModel.requestApplyAll()
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.errorMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (effectiveDarkTheme) {
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            )
    ) {
        if (viewModel.currentBitmap == null && !viewModel.isLoading) {
            IconButton(
                onClick = { onThemeModeChange(themeMode.next()) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(
                        if (effectiveDarkTheme) {
                            Color.White.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        },
                        shape = CircleShape
                    )
            ) {
                val icon = when (themeMode) {
                    ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                    ThemeMode.LIGHT -> Icons.Default.LightMode
                    ThemeMode.DARK -> Icons.Default.DarkMode
                }
                val label = when (themeMode) {
                    ThemeMode.SYSTEM -> "Тема: системная"
                    ThemeMode.LIGHT -> "Тема: светлая"
                    ThemeMode.DARK -> "Тема: тёмная"
                }
            }
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = if (effectiveDarkTheme) Color.White else MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Редактор\nфото",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = if (effectiveDarkTheme) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Выберите фото для начала редактирования",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (effectiveDarkTheme) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        onClick = { onImageSelected(android.net.Uri.EMPTY) },
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (effectiveDarkTheme) Color.White else MaterialTheme.colorScheme.primary,
                            contentColor = if (effectiveDarkTheme) MaterialTheme.colorScheme.primary else Color.White
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Открыть фото", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            if (viewModel.showCropMode && viewModel.originalBitmap != null) {
                CropScreen(
                    bitmap = viewModel.getBitmapForCrop() ?: viewModel.originalBitmap!!,
                    onCropComplete = { rect ->
                        AppLog.i(log, "cropComplete rect=$rect rot=${viewModel.rotation}")
                        viewModel.showCropMode = false
                        viewModel.cropRect = rect
                        viewModel.cropRectRotation = viewModel.rotation
                        viewModel.applyAll()
                        viewModel.saveToHistory()
                    },
                    onCancel = {
                        AppLog.i(log, "cropCancel")
                        viewModel.showCropMode = false
                    },
                    aspectRatio = viewModel.selectedCropRatio
                )
            } else {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    viewModel.currentBitmap?.let { bitmap ->
                        val imageBitmap = bitmap.asImageBitmap()
                        val imageWidth = bitmap.width.toFloat()
                        val imageHeight = bitmap.height.toFloat()
                        val screenWidth = constraints.maxWidth.toFloat()
                        val screenHeight = constraints.maxHeight.toFloat()

                        val scaleX = screenWidth / imageWidth
                        val scaleY = screenHeight / imageHeight
                        val scale = minOf(scaleX, scaleY)

                        val scaledWidth = imageWidth * scale
                        val scaledHeight = imageHeight * scale

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            viewModel.showOriginal = true
                                            tryAwaitRelease()
                                            viewModel.showOriginal = false
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = null,
                                modifier = Modifier
                                    .width(scaledWidth.dp)
                                    .height(scaledHeight.dp),
                                contentScale = ContentScale.Fit
                            )

                            AnimatedVisibility(
                                visible = viewModel.showOriginal && viewModel.originalBitmap != null,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                viewModel.originalBitmap?.let { original ->
                                    val originalImageBitmap = original.asImageBitmap()
                                    val originalWidth = original.width.toFloat()
                                    val originalHeight = original.height.toFloat()

                                    val originalScaleX = screenWidth / originalWidth
                                    val originalScaleY = screenHeight / originalHeight
                                    val originalScale = minOf(originalScaleX, originalScaleY)

                                    val originalScaledWidth = originalWidth * originalScale
                                    val originalScaledHeight = originalHeight * originalScale

                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            bitmap = originalImageBitmap,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .width(originalScaledWidth.dp)
                                                .height(originalScaledHeight.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter),
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onImageSelected(android.net.Uri.EMPTY) },
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                                shape = CircleShape
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.PhotoLibrary,
                                            "Выбрать фото",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = "Редактор фото",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onThemeModeChange(themeMode.next()) },
                                            modifier = Modifier.background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                                shape = CircleShape
                                            )
                                        ) {
                                            val icon = when (themeMode) {
                                                ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                                ThemeMode.DARK -> Icons.Default.DarkMode
                                            }
                                            val label = when (themeMode) {
                                                ThemeMode.SYSTEM -> "Тема: системная"
                                                ThemeMode.LIGHT -> "Тема: светлая"
                                                ThemeMode.DARK -> "Тема: тёмная"
                                            }
                                            Icon(icon, label, tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(
                                            onClick = { viewModel.showOriginal = !viewModel.showOriginal },
                                            enabled = viewModel.currentBitmap != null,
                                            modifier = Modifier
                                                .background(
                                                    if (viewModel.showOriginal) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                                    shape = CircleShape
                                                )
                                        ) {
                                            Icon(
                                                if (viewModel.showOriginal) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                "Сравнить",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (viewModel.historyIndex > 0) {
                                                    viewModel.historyIndex--
                                                    viewModel.restoreState(viewModel.history[viewModel.historyIndex])
                                                }
                                            },
                                            enabled = viewModel.historyIndex > 0,
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                                    shape = CircleShape
                                                )
                                        ) {
                                            Icon(Icons.Default.Undo, "Отменить", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(
                                            onClick = {
                                                if (viewModel.historyIndex < viewModel.history.size - 1) {
                                                    viewModel.historyIndex++
                                                    viewModel.restoreState(viewModel.history[viewModel.historyIndex])
                                                }
                                            },
                                            enabled = viewModel.historyIndex < viewModel.history.size - 1,
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                                    shape = CircleShape
                                                )
                                        ) {
                                            Icon(Icons.Default.Redo, "Повторить", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            if (viewModel.isLoading) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(48.dp),
                                            strokeWidth = 4.dp
                                        )
                                        Text(
                                            "Обработка...",
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(),
                                color = ToolPanelPurpleDark,
                                shadowElevation = 16.dp,
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            ) {
                                val scrollState = rememberScrollState()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 20.dp)
                                        .horizontalScroll(scrollState),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ToolButton(
                                        icon = Icons.Default.AutoFixHigh,
                                        label = "Авто",
                                        selected = viewModel.autoEnhanceEnabled,
                                        onClick = {
                                            viewModel.autoEnhanceEnabled = !viewModel.autoEnhanceEnabled
                                            viewModel.saveToHistoryIfChanged()
                                        }
                                    )
                                    ToolButton(
                                        icon = Icons.Default.Palette,
                                        label = "Фильтры",
                                        selected = viewModel.selectedTool == ToolType.FILTERS,
                                        onClick = {
                                            if (viewModel.selectedTool == ToolType.FILTERS) {
                                                viewModel.openTool(ToolType.NONE)
                                            } else {
                                                viewModel.openTool(ToolType.FILTERS)
                                            }
                                        }
                                    )
                                    ToolButton(
                                        icon = Icons.Default.Tune,
                                        label = "Настройки",
                                        selected = viewModel.selectedTool == ToolType.ADJUST,
                                        onClick = {
                                            if (viewModel.selectedTool == ToolType.ADJUST) {
                                                viewModel.openTool(ToolType.NONE)
                                            } else {
                                                viewModel.openTool(ToolType.ADJUST)
                                            }
                                        }
                                    )
                                    ToolButton(
                                        icon = Icons.Default.GraphicEq,
                                        label = "Кривые",
                                        selected = viewModel.selectedTool == ToolType.CURVES,
                                        onClick = {
                                            if (viewModel.selectedTool == ToolType.CURVES) {
                                                viewModel.openTool(ToolType.NONE)
                                            } else {
                                                viewModel.openTool(ToolType.CURVES)
                                            }
                                        }
                                    )
                                    ToolButton(
                                        icon = Icons.Default.Crop,
                                        label = "Обрезка",
                                        selected = viewModel.selectedTool == ToolType.CROP,
                                        onClick = {
                                            if (viewModel.selectedTool == ToolType.CROP) {
                                                viewModel.showCropMode = false
                                                viewModel.selectedTool = ToolType.NONE
                                            } else {
                                                viewModel.openTool(ToolType.CROP)
                                            }
                                        }
                                    )
                                    ToolButton(
                                        icon = Icons.Default.RotateRight,
                                        label = "Поворот",
                                        selected = viewModel.selectedTool == ToolType.ROTATE,
                                        onClick = {
                                            if (viewModel.selectedTool == ToolType.ROTATE) {
                                                viewModel.openTool(ToolType.NONE)
                                            } else {
                                                viewModel.openTool(ToolType.ROTATE)
                                            }
                                        }
                                    )
                                    ToolButton(
                                        icon = Icons.Default.Bookmark,
                                        label = "Пресеты",
                                        selected = viewModel.selectedTool == ToolType.PRESETS,
                                        onClick = {
                                            if (viewModel.selectedTool == ToolType.PRESETS) {
                                                viewModel.openTool(ToolType.NONE)
                                            } else {
                                                viewModel.openTool(ToolType.PRESETS)
                                            }
                                        }
                                    )
                                    ToolButton(
                                        icon = Icons.Default.TextFields,
                                        label = "Текст",
                                        selected = !viewModel.textOverlay.isNullOrBlank(),
                                        onClick = {
                                            viewModel.openTextEditor()
                                        }
                                    )
                                    ToolButton(
                                        icon = Icons.Default.AutoAwesome,
                                        label = "Эффекты",
                                        selected = viewModel.selectedTool == ToolType.EFFECTS,
                                        onClick = {
                                            if (viewModel.selectedTool == ToolType.EFFECTS) {
                                                viewModel.openTool(ToolType.NONE)
                                            } else {
                                                viewModel.openTool(ToolType.EFFECTS)
                                            }
                                        }
                                    )
                                    FloatingActionButton(
                                        onClick = {
                                            viewModel.showExportDialog = true
                                        },
                                        modifier = Modifier.size(64.dp),
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                        elevation = FloatingActionButtonDefaults.elevation(
                                            defaultElevation = 8.dp,
                                            pressedElevation = 12.dp
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Save,
                                            "Сохранить",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (viewModel.showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.showBottomSheet = false
                    viewModel.selectedTool = ToolType.NONE
                    viewModel.saveToHistoryIfChanged()
                    scope.launch {
                        sheetState.hide()
                    }
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            ) {
                when (viewModel.selectedTool) {
                    ToolType.FILTERS -> FiltersSheet(
                        filters = FilterProvider.filters.keys.toList(),
                        currentFilter = viewModel.currentFilterName,
                        onFilterSelected = {
                            viewModel.currentFilterName = it
                            viewModel.saveToHistory()
                        }
                    )
                    ToolType.ADJUST -> AdjustSheet(
                        brightness = viewModel.brightness,
                        contrast = viewModel.contrast,
                        saturation = viewModel.saturation,
                        warmth = viewModel.warmth,
                        sharpness = viewModel.sharpness,
                        onBrightnessChange = { viewModel.brightness = it },
                        onContrastChange = { viewModel.contrast = it },
                        onSaturationChange = { viewModel.saturation = it },
                        onWarmthChange = { viewModel.warmth = it },
                        onSharpnessChange = { viewModel.sharpness = it },
                        onApplyFinished = { viewModel.saveToHistoryIfChanged() }
                    )
                    ToolType.CURVES -> CurvesSheet(
                        curveMaster = viewModel.curveMaster,
                        curveR = viewModel.curveR,
                        curveG = viewModel.curveG,
                        curveB = viewModel.curveB,
                        onCurveMasterChange = { viewModel.updateCurveMaster(it) },
                        onCurveRChange = { viewModel.updateCurveR(it) },
                        onCurveGChange = { viewModel.updateCurveG(it) },
                        onCurveBChange = { viewModel.updateCurveB(it) },
                        onApplyFinished = { viewModel.saveToHistoryIfChanged() }
                    )
                    ToolType.ROTATE -> RotateSheet(
                        rotation = viewModel.rotation,
                        onRotationChange = {
                            viewModel.rotation = it
                        },
                        onRotate90 = {
                            viewModel.rotation = (viewModel.rotation - 90f + 360f) % 360f
                            viewModel.saveToHistoryIfChanged()
                        },
                        onRotate90Clockwise = {
                            viewModel.rotation = (viewModel.rotation + 90f) % 360f
                            viewModel.saveToHistoryIfChanged()
                        },
                        flipHorizontal = viewModel.flipHorizontal,
                        flipVertical = viewModel.flipVertical,
                        onFlipHorizontal = {
                            viewModel.flipHorizontal = !viewModel.flipHorizontal
                            viewModel.saveToHistoryIfChanged()
                        },
                        onFlipVertical = {
                            viewModel.flipVertical = !viewModel.flipVertical
                            viewModel.saveToHistoryIfChanged()
                        }
                    )
                    ToolType.PRESETS -> PresetsSheet(
                        presets = viewModel.presets,
                        onPresetSelected = { viewModel.applyPreset(it) },
                        onSavePreset = { name -> viewModel.savePreset(name) }
                    )
                    ToolType.EFFECTS -> EffectsSheet(
                        effects = FilterProvider.effects.keys.toList(),
                        currentEffect = viewModel.currentEffectName,
                        onEffectSelected = {
                            viewModel.currentEffectName = if (it == viewModel.currentEffectName) null else it
                            viewModel.saveToHistory()
                        }
                    )
                    else -> {}
                }
            }
        }

        if (viewModel.showExportDialog) {
            ExportDialog(
                format = viewModel.exportFormat,
                quality = viewModel.exportQuality,
                onFormatChange = { viewModel.exportFormat = it },
                onQualityChange = { viewModel.exportQuality = it },
                onExport = {
                    viewModel.exportImage(
                        onSuccess = {
                            Toast.makeText(context, "Изображение сохранено", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            viewModel.errorMessage = error
                        }
                    )
                },
                onDismiss = { viewModel.showExportDialog = false }
            )
        }

        if (viewModel.showTextDialog) {
            var textInput by remember(viewModel.showTextDialog, viewModel.textOverlay) {
                mutableStateOf(viewModel.textOverlay.orEmpty())
            }
            var textSizeScale by remember(viewModel.showTextDialog, viewModel.textSizeScale) {
                mutableFloatStateOf(viewModel.textSizeScale)
            }
            var textBold by remember(viewModel.showTextDialog, viewModel.textBold) {
                mutableStateOf(viewModel.textBold)
            }
            var textFont by remember(viewModel.showTextDialog, viewModel.textFont) {
                mutableStateOf(viewModel.textFont)
            }
            AlertDialog(
                onDismissRequest = { viewModel.showTextDialog = false },
                title = { Text("Текст на фото") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            singleLine = false,
                            maxLines = 3,
                            placeholder = { Text("Введите текст") }
                        )
                        Text("Размер: ${(textSizeScale * 100).toInt()}%")
                        Slider(
                            value = textSizeScale,
                            onValueChange = { textSizeScale = it },
                            valueRange = 0.03f..1.0f
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Жирный")
                            Switch(
                                checked = textBold,
                                onCheckedChange = { textBold = it }
                            )
                        }
                        Text("Шрифт")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = textFont == TextFont.SANS,
                                onClick = { textFont = TextFont.SANS },
                                label = { Text("Sans", fontFamily = FontFamily.SansSerif) }
                            )
                            FilterChip(
                                selected = textFont == TextFont.SERIF,
                                onClick = { textFont = TextFont.SERIF },
                                label = { Text("Serif", fontFamily = FontFamily.Serif) }
                            )
                            FilterChip(
                                selected = textFont == TextFont.MONO,
                                onClick = { textFont = TextFont.MONO },
                                label = { Text("Mono", fontFamily = FontFamily.Monospace) }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.applyTextOverlayText(
                                newText = textInput,
                                sizeScale = textSizeScale,
                                bold = textBold,
                                font = textFont
                            )
                        }
                    ) {
                        Text("Применить")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.showTextDialog = false }
                    ) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "PhotoEditor - Empty", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun PhotoEditorScreenPreviewEmptyLight() {
    PhotoEditorMobileTheme(darkTheme = false, dynamicColor = false) {
        val vm = remember { PhotoEditorViewModel() }
        PhotoEditorScreen(
            viewModel = vm,
            onImageSelected = {},
            themeMode = ThemeMode.LIGHT,
            onThemeModeChange = {}
        )
    }
}

@Preview(showBackground = true, name = "PhotoEditor - Empty (Dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PhotoEditorScreenPreviewEmptyDark() {
    PhotoEditorMobileTheme(darkTheme = true, dynamicColor = false) {
        val vm = remember { PhotoEditorViewModel() }
        PhotoEditorScreen(
            viewModel = vm,
            onImageSelected = {},
            themeMode = ThemeMode.DARK,
            onThemeModeChange = {}
        )
    }
}

@Preview(showBackground = true, name = "PhotoEditor - With Image", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun PhotoEditorScreenPreviewWithImageLight() {
    PhotoEditorMobileTheme(darkTheme = false, dynamicColor = false) {
        val context = LocalContext.current
        val vm = remember {
            PhotoEditorViewModel().apply {
                initialize(context)
                setImageForPreview(PreviewBitmaps.sample())
            }
        }
        PhotoEditorScreen(
            viewModel = vm,
            onImageSelected = {},
            themeMode = ThemeMode.LIGHT,
            onThemeModeChange = {}
        )
    }
}

@Preview(showBackground = true, name = "PhotoEditor - With Image (Dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PhotoEditorScreenPreviewWithImageDark() {
    PhotoEditorMobileTheme(darkTheme = true, dynamicColor = false) {
        val context = LocalContext.current
        val vm = remember {
            PhotoEditorViewModel().apply {
                initialize(context)
                setImageForPreview(PreviewBitmaps.sample())
            }
        }
        PhotoEditorScreen(
            viewModel = vm,
            onImageSelected = {},
            themeMode = ThemeMode.DARK,
            onThemeModeChange = {}
        )
    }
}