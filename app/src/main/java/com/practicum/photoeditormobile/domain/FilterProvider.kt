package com.practicum.photoeditormobile.domain

import jp.co.cyberagent.android.gpuimage.filter.*
import android.util.Log

object FilterProvider {
    val filters by lazy<Map<String, () -> GPUImageFilter>> {
        mapOf(
            "Нет" to { GPUImageFilter() },
            "Сепия" to { GPUImageSepiaToneFilter() },
            "Черно-белый" to { GPUImageGrayscaleFilter() },
            "Негатив" to { GPUImageColorInvertFilter() },
            "Постеризация" to { GPUImagePosterizeFilter(4) },
            "Эскиз" to { GPUImageSketchFilter() },
            "Винтажный" to { GPUImageVignetteFilter() },
            "Мягкий свет" to { GPUImageGaussianBlurFilter(2f) },
            "Кларендон" to { GPUImageBrightnessFilter(0.1f) },
            "Рейес" to { GPUImageSepiaToneFilter(0.6f) },
            "Луна" to { GPUImageGrayscaleFilter() },
            "Ларк" to { GPUImageBrightnessFilter(0.05f) },
            "Гингам" to { GPUImageSepiaToneFilter(0.3f) }
        )
    }

    val effects by lazy<Map<String, () -> GPUImageFilter>> {
        mapOf(
            "Виньетка" to { createSoftVignette() },
            "Зерно" to { GPUImageSharpenFilter(0.8f) },
            "Радиальное размытие" to { GPUImageGaussianBlurFilter(3f) },
            "Мягкий фокус" to { GPUImageGaussianBlurFilter(1.5f) },
            "Сильная виньетка" to { GPUImageVignetteFilter() }
        )
    }

    private fun createSoftVignette(): GPUImageVignetteFilter {
        return GPUImageVignetteFilter().apply {
            try {
                javaClass.getDeclaredMethod("setVignetteCenter", Float::class.java, Float::class.java)
                    .invoke(this, 0.5f, 0.5f)
                javaClass.getDeclaredMethod("setVignetteStart", Float::class.java)
                    .invoke(this, 0.5f)
                javaClass.getDeclaredMethod("setVignetteEnd", Float::class.java)
                    .invoke(this, 0.8f)
            } catch (e: Exception) {
                Log.e("FilterProvider", "Error setting soft vignette parameters", e)
            }
        }
    }
}