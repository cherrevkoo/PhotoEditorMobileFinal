package com.practicum.photoeditormobile.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import com.practicum.photoeditormobile.data.Curve3
import com.practicum.photoeditormobile.utils.AppLog
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class ImageProcessor(private val context: Context) {
    private companion object {
        const val LOG = "Processor"
    }
    
    private fun createWarmthFilter(warmth: Float): GPUImageWhiteBalanceFilter? {
        if (warmth == 0f) return null
        val temperature = (5000f + (warmth.coerceIn(-50f, 50f) * 50f)).coerceIn(2500f, 7500f)
        return GPUImageWhiteBalanceFilter(temperature, 0f)
    }
    
    suspend fun applyFilters(
        bitmap: Bitmap,
        baseFilter: GPUImageFilter,
        effectFilter: GPUImageFilter?,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        sharpness: Float,
        warmth: Float,
        curveMaster: Curve3 = Curve3(),
        curveR: Curve3 = Curve3(),
        curveG: Curve3 = Curve3(),
        curveB: Curve3 = Curve3()
    ): Bitmap? {
        return withContext(Dispatchers.Default) {
            try {
                val t0 = System.nanoTime()
                val argbBitmap =
                    if (bitmap.config == Bitmap.Config.ARGB_8888) bitmap
                    else bitmap.copy(Bitmap.Config.ARGB_8888, true)

                val safeBrightness = brightness.coerceIn(-1f, 1f)
                val safeContrast = contrast.coerceIn(0f, 4f)
                val safeSaturation = saturation.coerceIn(0f, 2f)
                val safeSharpness = sharpness.coerceIn(0f, 10f)
                val warmthFilter = createWarmthFilter(AdjustMappings.warmthSafe(warmth))

                val curveFilters = buildToneCurveFilters(curveMaster, curveR, curveG, curveB)
                val group = GPUImageFilterGroup().apply {
                    addFilter(baseFilter)
                    if (warmthFilter != null) addFilter(warmthFilter)
                    if (effectFilter != null) addFilter(effectFilter)
                    curveFilters.forEach { addFilter(it) }
                    addFilter(GPUImageBrightnessFilter(safeBrightness))
                    addFilter(GPUImageContrastFilter(safeContrast))
                    addFilter(GPUImageSaturationFilter(safeSaturation))
                    // sharpness: 0..10 -> GPUImageSharpenFilter expects roughly -4..4. Use 0..4.
                    addFilter(GPUImageSharpenFilter(AdjustMappings.sharpnessToGpu(safeSharpness)))
                }
                AppLog.d(
                    LOG,
                    "applyFilters(bmp=${argbBitmap.width}x${argbBitmap.height} cfg=${argbBitmap.config} " +
                        "base=${baseFilter.javaClass.simpleName} effect=${effectFilter?.javaClass?.simpleName} " +
                        "b=$safeBrightness c=$safeContrast s=$safeSaturation sh=$safeSharpness w=$warmth " +
                        "curves=${!curveMaster.isIdentity() || !curveR.isIdentity() || !curveG.isIdentity() || !curveB.isIdentity()} " +
                        "m=${curveMaster.shadows},${curveMaster.midtones},${curveMaster.highlights} " +
                        "rgb=(${curveR.shadows},${curveR.midtones},${curveR.highlights})" +
                        "/(${curveG.shadows},${curveG.midtones},${curveG.highlights})" +
                        "/(${curveB.shadows},${curveB.midtones},${curveB.highlights})"
                )

                val gpu = GPUImage(context).apply {
                    setImage(argbBitmap)
                    setFilter(group)
                }
                val out = gpu.bitmapWithFilterApplied
                val elapsedMs = ((System.nanoTime() - t0) / 1_000_000.0).roundToInt()

                val inSum = sampleRgbSum(argbBitmap)
                val outSum = sampleRgbSum(out)
                if (inSum > 60 && outSum <= 5) {
                    AppLog.e(
                        LOG,
                        "applyFilters produced near-black output; returning input. inSum=$inSum outSum=$outSum elapsed=${elapsedMs}ms " +
                            "curves m=${curveMaster.shadows},${curveMaster.midtones},${curveMaster.highlights} " +
                            "r=${curveR.shadows},${curveR.midtones},${curveR.highlights} " +
                            "g=${curveG.shadows},${curveG.midtones},${curveG.highlights} " +
                            "b=${curveB.shadows},${curveB.midtones},${curveB.highlights}"
                    )
                    argbBitmap
                } else {
                    AppLog.d(LOG, "applyFilters done -> ${out.width}x${out.height} sum=$outSum elapsed=${elapsedMs}ms")
                    out
                }
            } catch (e: Exception) {
                AppLog.e(LOG, "applyFilters exception: ${e.message}", e)
                bitmap
            }
        }
    }
    private fun buildToneCurveFilters(master: Curve3, r: Curve3, g: Curve3, b: Curve3): List<GPUImageToneCurveFilter> {
        if (master.isIdentity() && r.isIdentity() && g.isIdentity() && b.isIdentity()) return emptyList()

        fun p(x: Float, y: Int) = PointF(x, (y.coerceIn(0, 255) / 255f))
        val xs = floatArrayOf(0f, 0.5f, 1f)
        fun identity3() = arrayOf(
            p(xs[0], 0),
            p(xs[1], 128),
            p(xs[2], 255)
        )

        val out = ArrayList<GPUImageToneCurveFilter>(2)

        if (!master.isIdentity()) {
            val masterOnly = GPUImageToneCurveFilter().apply {
                setRgbCompositeControlPoints(
                    arrayOf(
                        p(xs[0], master.shadows),
                        p(xs[1], master.midtones),
                        p(xs[2], master.highlights)
                    )
                )
                setRedControlPoints(identity3())
                setGreenControlPoints(identity3())
                setBlueControlPoints(identity3())
            }
            out.add(masterOnly)
        }

        val needsChannel = !r.isIdentity() || !g.isIdentity() || !b.isIdentity()
        if (needsChannel) {
            val mLut = CurvesLut.buildLut(master)
            fun composeChannelDelta(channelCurve: Curve3): Curve3 {
                if (channelCurve.isIdentity()) return Curve3()
                val chLut = CurvesLut.buildLut(channelCurve)
                fun yAt(xIdx: Int): Int {
                    val afterMaster = mLut[xIdx]
                    val target = chLut[afterMaster]
                    val delta = (target - afterMaster).coerceIn(-255, 255)
                    val y = (xIdx + delta).coerceIn(0, 255)
                    return y
                }
                return Curve3(shadows = yAt(0), midtones = yAt(128), highlights = yAt(255))
            }

            val dr = composeChannelDelta(r)
            val dg = composeChannelDelta(g)
            val db = composeChannelDelta(b)

            if (!dr.isIdentity() || !dg.isIdentity() || !db.isIdentity()) {
                val channelOnly = GPUImageToneCurveFilter().apply {
                    setRgbCompositeControlPoints(identity3())
                    setRedControlPoints(
                        arrayOf(
                            p(xs[0], dr.shadows),
                            p(xs[1], dr.midtones),
                            p(xs[2], dr.highlights)
                        )
                    )
                    setGreenControlPoints(
                        arrayOf(
                            p(xs[0], dg.shadows),
                            p(xs[1], dg.midtones),
                            p(xs[2], dg.highlights)
                        )
                    )
                    setBlueControlPoints(
                        arrayOf(
                            p(xs[0], db.shadows),
                            p(xs[1], db.midtones),
                            p(xs[2], db.highlights)
                        )
                    )
                }
                out.add(channelOnly)
            }
        }

        return out
    }

    private fun sampleRgbSum(bitmap: Bitmap): Int {
        if (bitmap.width <= 0 || bitmap.height <= 0) return 0
        val xs = intArrayOf(0, bitmap.width / 2, bitmap.width - 1)
        val ys = intArrayOf(0, bitmap.height / 2, bitmap.height - 1)
        var sum = 0
        for (y in ys) {
            for (x in xs) {
                val p = bitmap.getPixel(x.coerceIn(0, bitmap.width - 1), y.coerceIn(0, bitmap.height - 1))
                sum += (p ushr 16) and 0xFF
                sum += (p ushr 8) and 0xFF
                sum += p and 0xFF
            }
        }
        return sum
    }
}




