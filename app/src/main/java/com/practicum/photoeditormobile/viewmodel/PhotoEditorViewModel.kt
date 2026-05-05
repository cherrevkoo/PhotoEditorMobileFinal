package com.practicum.photoeditormobile.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.photoeditormobile.data.*
import com.practicum.photoeditormobile.domain.AdjustMappings
import com.practicum.photoeditormobile.domain.AutoEnhancer
import com.practicum.photoeditormobile.domain.FilterProvider
import com.practicum.photoeditormobile.domain.ImageProcessor
import com.practicum.photoeditormobile.utils.AppLog
import com.practicum.photoeditormobile.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoEditorViewModel : ViewModel() {
    private companion object {
        const val LOG = "VM"
    }
    
    var originalBitmap by mutableStateOf<Bitmap?>(null)
        private set
    var rotatedBitmap by mutableStateOf<Bitmap?>(null)
        private set
    var currentBitmap by mutableStateOf<Bitmap?>(null)
        private set
    var showOriginal by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var showCropMode by mutableStateOf(false)
    var cropRect by mutableStateOf<CropRect?>(null)
    var cropRectRotation by mutableFloatStateOf(0f)
    var selectedTool by mutableStateOf<ToolType>(ToolType.NONE)
    var showBottomSheet by mutableStateOf(false)
    
    val history = mutableStateListOf<EditState>()
    var historyIndex by mutableIntStateOf(-1)
    private var isRestoringState = false
    
    var currentFilterName by mutableStateOf("Нет")
    var currentEffectName by mutableStateOf<String?>(null)
    var brightness by mutableFloatStateOf(50f)
    var contrast by mutableFloatStateOf(1f)
    var saturation by mutableFloatStateOf(1f)
    var warmth by mutableFloatStateOf(0f)
    var sharpness by mutableFloatStateOf(0f)
    var autoEnhanceEnabled by mutableStateOf(false)
    var curveMaster by mutableStateOf(Curve3())
    var curveR by mutableStateOf(Curve3())
    var curveG by mutableStateOf(Curve3())
    var curveB by mutableStateOf(Curve3())
    var curvesVersion by mutableIntStateOf(0)
    var rotation by mutableFloatStateOf(0f)
    var flipHorizontal by mutableStateOf(false)
    var flipVertical by mutableStateOf(false)
    var textOverlay by mutableStateOf<String?>(null)
    var textSizeScale by mutableFloatStateOf(0.08f)
    var textBold by mutableStateOf(false)
    var textFont by mutableStateOf(TextFont.SANS)
    var showTextDialog by mutableStateOf(false)
    
    val presets = mutableStateListOf<Preset>()
    var showExportDialog by mutableStateOf(false)
    var exportFormat by mutableStateOf(ExportFormat.JPEG)
    var exportQuality by mutableIntStateOf(95)
    var selectedCropRatio by mutableStateOf(CropAspectRatio.FREE)
    
    private lateinit var imageProcessor: ImageProcessor
    private lateinit var appContext: Context

    private var applyJob: Job? = null
    private var loadJob: Job? = null
    private var exportJob: Job? = null
    private var debounceApplyJob: Job? = null
    
    fun requestApplyAll(debounceMs: Long = 60L) {
        debounceApplyJob?.cancel()
        debounceApplyJob = viewModelScope.launch {
            if (debounceMs > 0) delay(debounceMs)
            applyAll()
        }
    }

    fun initialize(context: Context) {
        val newAppContext = context.applicationContext
        if (::appContext.isInitialized && appContext === newAppContext) return
        this.appContext = newAppContext
        this.imageProcessor = ImageProcessor(appContext)
        AppLog.i(LOG, "initialize(appContext=${appContext.packageName})")
    }
    
    fun loadImage(uri: Uri) {
        loadJob?.cancel()
        applyJob?.cancel()
        exportJob?.cancel()
        loadJob = viewModelScope.launch {
            isLoading = true
            errorMessage = null
            AppLog.i(LOG, "loadImage(uri=$uri)")
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapUtils.decodeSampledBitmapFromUri(
                        appContext,
                        uri,
                        2048,
                        2048
                    )
                }
                if (bitmap != null) {
                    originalBitmap = bitmap
                    rotatedBitmap = null
                    currentBitmap = bitmap
                    rotation = 0f
                    cropRect = null
                    cropRectRotation = 0f
                    currentFilterName = "Нет"
                    currentEffectName = null
                    brightness = 50f
                    contrast = 1f
                    saturation = 1f
                    warmth = 0f
                    sharpness = 0f
                    autoEnhanceEnabled = false
                    curveMaster = Curve3()
                    curveR = Curve3()
                    curveG = Curve3()
                    curveB = Curve3()
                    curvesVersion = 0
                    flipHorizontal = false
                    flipVertical = false
                    textOverlay = null
                    textSizeScale = 0.08f
                    textBold = false
                    textFont = TextFont.SANS
                    history.clear()
                    historyIndex = -1
                    saveToHistory()
                    AppLog.i(LOG, "loadImage success: ${bitmap.width}x${bitmap.height}")
                } else {
                    errorMessage = "Не удалось загрузить изображение"
                    AppLog.w(LOG, "loadImage failed: decoded bitmap is null")
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки: ${e.message}"
                AppLog.e(LOG, "loadImage exception: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    @VisibleForTesting
    internal fun setImageForTest(bitmap: Bitmap) {
        loadJob?.cancel()
        applyJob?.cancel()
        exportJob?.cancel()

        originalBitmap = bitmap
        rotatedBitmap = null
        currentBitmap = bitmap
        rotation = 0f
        cropRect = null
        cropRectRotation = 0f
        currentFilterName = "Нет"
        currentEffectName = null
        brightness = 50f
        contrast = 1f
        saturation = 1f
        warmth = 0f
        sharpness = 0f
        autoEnhanceEnabled = false
        curveMaster = Curve3()
        curveR = Curve3()
        curveG = Curve3()
        curveB = Curve3()
        curvesVersion = 0
        flipHorizontal = false
        flipVertical = false
        textOverlay = null
        textSizeScale = 0.08f
        textBold = false
        textFont = TextFont.SANS
        history.clear()
        historyIndex = -1
        saveToHistory()
    }

    fun updateCurveMaster(newCurve: Curve3) {
        curveMaster = newCurve
        curvesVersion++
    }

    fun updateCurveR(newCurve: Curve3) {
        curveR = newCurve
        curvesVersion++
    }

    fun updateCurveG(newCurve: Curve3) {
        curveG = newCurve
        curvesVersion++
    }

    fun updateCurveB(newCurve: Curve3) {
        curveB = newCurve
        curvesVersion++
    }

    fun setImageForPreview(bitmap: Bitmap) {
        setImageForTest(bitmap)
    }
    
    fun applyAll() {
        val baseBitmap = originalBitmap
        if (baseBitmap == null) {
            currentBitmap = null
            return
        }

        applyJob?.cancel()

        AppLog.d(
            LOG,
            "applyAll start: base=${baseBitmap.width}x${baseBitmap.height} " +
                "flipH=$flipHorizontal flipV=$flipVertical rot=$rotation crop=${cropRect != null} " +
                "filter=$currentFilterName effect=$currentEffectName " +
                "b=$brightness c=$contrast s=$saturation w=$warmth sh=$sharpness text=$textOverlay " +
                "textSize=$textSizeScale textBold=$textBold textFont=$textFont"
        )
        
        if (cropRect != null && cropRectRotation != rotation) {
            cropRect = null
            cropRectRotation = rotation
            AppLog.w(LOG, "applyAll: cropRect reset due rotation change (cropRectRotation != rotation)")
        }
        
        var transformedBitmap: Bitmap? = null
        
        try {
            var workingBitmap: Bitmap? = baseBitmap
            
            if (flipHorizontal || flipVertical) {
                workingBitmap = BitmapUtils.flipBitmap(workingBitmap, flipHorizontal, flipVertical)
                if (workingBitmap == null) {
                    errorMessage = "Ошибка отражения изображения"
                    currentBitmap = currentBitmap ?: baseBitmap
                    rotatedBitmap = null
                    AppLog.e(LOG, "applyAll: flipBitmap returned null")
                    return
                }
            }
            
            if (rotation != 0f && rotation % 360f != 0f) {
                workingBitmap = BitmapUtils.rotateBitmap(workingBitmap, rotation)
                if (workingBitmap == null) {
                    errorMessage = "Ошибка поворота изображения"
                    currentBitmap = currentBitmap ?: baseBitmap
                    rotatedBitmap = null
                    AppLog.e(LOG, "applyAll: rotateBitmap returned null (rotation=$rotation)")
                    return
                }
            }
            
            val crop = cropRect
            if (crop != null) {
                workingBitmap = BitmapUtils.cropBitmap(workingBitmap, crop)
                if (workingBitmap == null) {
                    errorMessage = "Ошибка обрезки изображения"
                    currentBitmap = currentBitmap ?: baseBitmap
                    rotatedBitmap = null
                    AppLog.e(LOG, "applyAll: cropBitmap returned null (crop=$crop)")
                    return
                }
            }
            
            transformedBitmap = workingBitmap
            rotatedBitmap = transformedBitmap
            
        } catch (e: OutOfMemoryError) {
            errorMessage = "Недостаточно памяти для трансформации"
            currentBitmap = baseBitmap
            rotatedBitmap = null
            AppLog.e(LOG, "applyAll OOM during transform", e)
            return
        } catch (e: Exception) {
            errorMessage = "Ошибка трансформации: ${e.message}"
            currentBitmap = baseBitmap
            rotatedBitmap = null
            AppLog.e(LOG, "applyAll exception during transform: ${e.message}", e)
            return
        }
        
        if (transformedBitmap == null) {
            currentBitmap = baseBitmap
            return
        }
        
        val hasFilters = currentFilterName != "Нет" || currentEffectName != null ||
            brightness != 50f || contrast != 1f || saturation != 1f ||
            warmth != 0f || sharpness != 0f || autoEnhanceEnabled ||
            !curveMaster.isIdentity() || !curveR.isIdentity() || !curveG.isIdentity() || !curveB.isIdentity() ||
            !textOverlay.isNullOrBlank()
        
        if (!hasFilters) {
            currentBitmap = applyTextOverlay(transformedBitmap)
            AppLog.d(LOG, "applyAll done: no filters -> ${transformedBitmap.width}x${transformedBitmap.height}")
            return
        }
        
        currentBitmap = transformedBitmap
        
        val bitmapForFilters = transformedBitmap
        val filterName = currentFilterName
        val effectName = currentEffectName
        val currentBrightness = brightness
        val currentContrast = contrast
        val currentSaturation = saturation
        val currentSharpness = sharpness
        val currentWarmth = warmth
        val autoEnhance = autoEnhanceEnabled
        val masterCurve = curveMaster
        val rCurve = curveR
        val gCurve = curveG
        val bCurve = curveB
        val currentTextOverlay = textOverlay
        val currentTextSizeScale = textSizeScale
        val currentTextBold = textBold
        val currentTextFont = textFont
        
        applyJob = viewModelScope.launch {
            isLoading = true
            try {
                val enhancedBitmap = withContext(Dispatchers.Default) {
                    if (autoEnhance) {
                        AppLog.d(LOG, "applyAll: autoEnhance start")
                        AutoEnhancer.autoEnhance(bitmapForFilters)
                    } else {
                        bitmapForFilters
                    }
                }

                val onlyAutoEnhance =
                    autoEnhance &&
                        filterName == "Нет" &&
                        effectName == null &&
                        currentBrightness == 50f &&
                        currentContrast == 1f &&
                        currentSaturation == 1f &&
                        currentWarmth == 0f &&
                        currentSharpness == 0f &&
                        masterCurve.isIdentity() &&
                        rCurve.isIdentity() &&
                        gCurve.isIdentity() &&
                        bCurve.isIdentity()

                if (onlyAutoEnhance) {
                    currentBitmap = applyTextOverlay(
                        bitmap = enhancedBitmap,
                        text = currentTextOverlay,
                        sizeScale = currentTextSizeScale,
                        bold = currentTextBold,
                        font = currentTextFont
                    )
                    AppLog.d(LOG, "applyAll done: autoEnhance-only -> ${enhancedBitmap.width}x${enhancedBitmap.height}")
                    return@launch
                }

                val baseFilter = FilterProvider.filters[filterName]?.invoke()
                    ?: jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter()
                val effectFilter = effectName?.let { FilterProvider.effects[it]?.invoke() }
                AppLog.d(LOG, "applyAll: applyFilters(auto=$autoEnhance filter=$filterName effect=$effectName)")
                
                val result = withContext(Dispatchers.Default) {
                    imageProcessor.applyFilters(
                        bitmap = enhancedBitmap,
                        baseFilter = baseFilter,
                        effectFilter = effectFilter,
                        brightness = AdjustMappings.brightnessUiToGpu(currentBrightness),
                        contrast = AdjustMappings.contrastSafe(currentContrast),
                        saturation = AdjustMappings.saturationSafe(currentSaturation),
                        sharpness = currentSharpness,
                        warmth = AdjustMappings.warmthSafe(currentWarmth),
                        curveMaster = masterCurve,
                        curveR = rCurve,
                        curveG = gCurve,
                        curveB = bCurve
                    )
                }
                
                currentBitmap = applyTextOverlay(
                    bitmap = result ?: enhancedBitmap,
                    text = currentTextOverlay,
                    sizeScale = currentTextSizeScale,
                    bold = currentTextBold,
                    font = currentTextFont
                )
                AppLog.d(LOG, "applyAll done: filtered -> ${currentBitmap?.width}x${currentBitmap?.height}")
            } catch (e: OutOfMemoryError) {
                errorMessage = "Недостаточно памяти для обработки изображения"
                currentBitmap = bitmapForFilters
                AppLog.e(LOG, "applyAll OOM during filters", e)
            } catch (e: CancellationException) {
                AppLog.d(LOG, "applyAll cancelled")
                return@launch
            } catch (e: Exception) {
                errorMessage = "Ошибка применения фильтров: ${e.message}"
                currentBitmap = bitmapForFilters
                AppLog.e(LOG, "applyAll exception during filters: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun getBitmapForCrop(): Bitmap? {
        val baseBitmap = originalBitmap ?: return null
        
        var transformedBitmap = baseBitmap
        transformedBitmap = BitmapUtils.flipBitmap(transformedBitmap, flipHorizontal, flipVertical) ?: transformedBitmap
        transformedBitmap = BitmapUtils.rotateBitmap(transformedBitmap, rotation) ?: transformedBitmap
        
        return transformedBitmap
    }
    
    fun saveToHistory() {
        if (isRestoringState) return
        
        val state = buildCurrentEditState()
        while (history.size > historyIndex + 1) {
            history.removeAt(history.lastIndex)
        }
        history.add(state)
        historyIndex = history.size - 1
        AppLog.d(LOG, "saveToHistory: size=${history.size} index=$historyIndex")
    }

    fun saveToHistoryIfChanged() {
        if (isRestoringState) return
        val state = buildCurrentEditState()
        val last = history.getOrNull(historyIndex)
        if (last == state) return
        saveToHistory()
    }

    private fun buildCurrentEditState(): EditState {
        return EditState(
            filterName = currentFilterName,
            effectName = currentEffectName,
            autoEnhance = autoEnhanceEnabled,
            brightness = brightness,
            contrast = contrast,
            saturation = saturation,
            warmth = warmth,
            sharpness = sharpness,
            curveMaster = curveMaster,
            curveR = curveR,
            curveG = curveG,
            curveB = curveB,
            rotation = rotation,
            cropRect = cropRect?.let { RectF(it.left, it.top, it.right, it.bottom) },
            flipHorizontal = flipHorizontal,
            flipVertical = flipVertical,
            textOverlay = textOverlay,
            textSizeScale = textSizeScale,
            textBold = textBold,
            textFont = textFont
        )
    }
    
    fun restoreState(state: EditState) {
        isRestoringState = true
        try {
            AppLog.i(LOG, "restoreState(index=$historyIndex)")
            currentFilterName = state.filterName
            currentEffectName = state.effectName
            autoEnhanceEnabled = state.autoEnhance
            brightness = state.brightness
            contrast = state.contrast
            saturation = state.saturation
            warmth = state.warmth
            sharpness = state.sharpness
            curveMaster = state.curveMaster
            curveR = state.curveR
            curveG = state.curveG
            curveB = state.curveB
            curvesVersion++
            rotation = state.rotation
            flipHorizontal = state.flipHorizontal
            flipVertical = state.flipVertical
            textOverlay = state.textOverlay
            textSizeScale = state.textSizeScale
            textBold = state.textBold
            textFont = state.textFont
            cropRect = state.cropRect?.let {
                CropRect(it.left, it.top, it.right, it.bottom)
            }
            cropRectRotation = rotation
            
            applyAll()
        } catch (e: Exception) {
            rotatedBitmap = null
            cropRect = null
            cropRectRotation = 0f
            if (originalBitmap != null) {
                currentBitmap = originalBitmap
            }
            errorMessage = "Ошибка восстановления состояния: ${e.message}"
            AppLog.e(LOG, "restoreState exception: ${e.message}", e)
        } finally {
            isRestoringState = false
        }
    }
    
    fun savePreset(name: String) {
        val preset = Preset(
            name = name,
            filterName = currentFilterName,
            brightness = brightness,
            contrast = contrast,
            saturation = saturation,
            warmth = warmth,
            sharpness = sharpness
        )
        presets.add(preset)
    }
    
    fun applyPreset(preset: Preset) {
        currentFilterName = preset.filterName
        brightness = preset.brightness
        contrast = preset.contrast
        saturation = preset.saturation
        warmth = preset.warmth
        sharpness = preset.sharpness
        saveToHistory()
    }
    
    fun exportImage(onSuccess: () -> Unit, onError: (String) -> Unit) {
        currentBitmap?.let { bmp ->
            exportJob?.cancel()
            exportJob = viewModelScope.launch {
                isLoading = true
                try {
                    AppLog.i(LOG, "exportImage(format=$exportFormat quality=$exportQuality bmp=${bmp.width}x${bmp.height})")
                    val success = withContext(Dispatchers.IO) {
                        ImageRepository.saveImageToGallery(
                            bmp,
                            appContext,
                            exportFormat,
                            exportQuality
                        )
                    }
                    if (success) {
                        onSuccess()
                        showExportDialog = false
                        AppLog.i(LOG, "exportImage success")
                    } else {
                        onError("Не удалось сохранить")
                        AppLog.w(LOG, "exportImage failed: saveImageToGallery=false")
                    }
                } catch (e: Exception) {
                    onError("Ошибка: ${e.message}")
                    AppLog.e(LOG, "exportImage exception: ${e.message}", e)
                } finally {
                    isLoading = false
                }
            }
        }
    }
    
    fun openTool(tool: ToolType) {
        selectedTool = tool
        when (tool) {
            ToolType.FILTERS, ToolType.ADJUST, ToolType.CURVES, ToolType.ROTATE, ToolType.PRESETS, ToolType.EFFECTS -> {
                showBottomSheet = true
            }
            ToolType.CROP -> {
                showCropMode = true
            }
            ToolType.NONE -> {
                showBottomSheet = false
            }
        }
    }

    fun openTextEditor() {
        if (originalBitmap == null) return
        showTextDialog = true
    }

    fun applyTextOverlayText(
        newText: String,
        sizeScale: Float = textSizeScale,
        bold: Boolean = textBold,
        font: TextFont = textFont
    ) {
        if (originalBitmap == null) return
        val normalizedText = newText.trim().ifBlank { null }
        val normalizedSize = sizeScale.coerceIn(0.03f, 1.0f)
        if (
            textOverlay == normalizedText &&
            textSizeScale == normalizedSize &&
            textBold == bold &&
            textFont == font
        ) {
            showTextDialog = false
            return
        }
        textOverlay = normalizedText
        textSizeScale = normalizedSize
        textBold = bold
        textFont = font
        showTextDialog = false
        applyAll()
        saveToHistoryIfChanged()
    }

    private fun applyTextOverlay(
        bitmap: Bitmap,
        text: String? = textOverlay,
        sizeScale: Float = textSizeScale,
        bold: Boolean = textBold,
        font: TextFont = textFont
    ): Bitmap {
        if (text.isNullOrBlank()) return bitmap
        return try {
            val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)
            val textSize = (
                mutableBitmap.width.coerceAtMost(mutableBitmap.height) * sizeScale.coerceIn(0.03f, 1.0f)
            ).coerceAtLeast(24f)
            val typefaceFamily = when (font) {
                TextFont.SANS -> Typeface.SANS_SERIF
                TextFont.SERIF -> Typeface.SERIF
                TextFont.MONO -> Typeface.MONOSPACE
            }
            val typefaceStyle = if (bold) Typeface.BOLD else Typeface.NORMAL

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                this.textSize = textSize
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(typefaceFamily, typefaceStyle)
            }
            val shadowPaint = Paint(textPaint).apply {
                color = Color.BLACK
            }

            val x = mutableBitmap.width / 2f
            val y = mutableBitmap.height * 0.9f
            canvas.drawText(text, x + 2f, y + 2f, shadowPaint)
            canvas.drawText(text, x, y, textPaint)
            mutableBitmap
        } catch (e: Exception) {
            AppLog.e(LOG, "applyTextOverlay exception: ${e.message}", e)
            bitmap
        }
    }

    override fun onCleared() {
        super.onCleared()
        AppLog.i(LOG, "onCleared()")
        applyJob?.cancel()
        debounceApplyJob?.cancel()
        loadJob?.cancel()
        exportJob?.cancel()
        originalBitmap = null
        rotatedBitmap = null
        currentBitmap = null
    }
}

