package com.google.android.exoplayer2.ui.utilities

import android.content.res.Resources

/**
 * Created by André Carvalho on 07/03/2019
 */

private val density
    get() = Resources.getSystem().displayMetrics.density

val Int.dp: Int
    get() = (this / density).toInt()
val Int.px: Int
    get() = (this * density).toInt()

/**
 * converts a pixel value to dp;
 */
val Float.dp: Float
    get() = (this / density)

/**
 * converts a dp value to px;
 */
val Float.px: Float
    get() = (this * density)
