package com.google.android.exoplayer2.ui.listeners

/**
 * Created by André Carvalho on 2019-10-30
 */
interface AnaliticsListener {
    fun on30SecsBack()
    fun on30SecsForw()
    fun onPause()
    fun onPlay()
}
