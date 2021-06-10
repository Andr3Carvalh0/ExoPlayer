package com.google.android.exoplayer2.ui.utilities

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.android.exoplayer2.ui.R

/**
 * Created by André Carvalho on 2019-12-31
 */
val Context.isTablet: Boolean
    get() = resources.getBoolean(R.bool.isTablet)

fun Context.screenWidth(): Int {
    val metrics = DisplayMetrics()
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager?

    wm?.defaultDisplay?.getMetrics(metrics)
    return metrics.widthPixels
}

fun Context.statusbar(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 24.px
}

fun Context.navigation(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
}

fun Context.screenHeight(): Int {
    val metrics = DisplayMetrics()
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager?

    wm?.defaultDisplay?.getMetrics(metrics)
    return metrics.heightPixels
}

val Context.isTallDevice: Boolean
    get() {
        val ratio: Float = this.screenRatio() ?: 1f
        return ratio.compareTo(16f / 9f) > 0
    }

fun Context.screenRatio(): Float? {
    return (screenWidth()?.toFloat()?.div(screenHeight()?.toFloat() ?: 1f))
}
