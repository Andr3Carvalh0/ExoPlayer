package com.google.android.exoplayer2.ui.listeners

/**
 * Created by André Carvalho on 15/10/2019
 */
public interface SwitchItemsListener {
    fun next()
    fun previous()

    fun hasNext(): Boolean
    fun hasPrevious(): Boolean
}
