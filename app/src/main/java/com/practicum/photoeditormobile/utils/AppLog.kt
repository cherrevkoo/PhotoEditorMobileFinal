package com.practicum.photoeditormobile.utils

import android.util.Log
import com.practicum.photoeditormobile.BuildConfig

object AppLog {
    private const val PREFIX = "PE"

    private fun tag(module: String) = "$PREFIX-$module"

    fun d(module: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag(module), message)
    }

    fun i(module: String, message: String) {
        if (BuildConfig.DEBUG) Log.i(tag(module), message)
    }

    fun w(module: String, message: String, tr: Throwable? = null) {
        if (!BuildConfig.DEBUG) return
        if (tr != null) Log.w(tag(module), message, tr) else Log.w(tag(module), message)
    }

    fun e(module: String, message: String, tr: Throwable? = null) {
        if (!BuildConfig.DEBUG) return
        if (tr != null) Log.e(tag(module), message, tr) else Log.e(tag(module), message)
    }
}

