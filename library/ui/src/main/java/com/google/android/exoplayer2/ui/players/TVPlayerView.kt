package com.google.android.exoplayer2.ui.players

import android.content.Context
import android.util.AttributeSet
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.TVPlayerControllerView

/**
 * Created by André Carvalho on 15/10/2019
 */
class TVPlayerView : PlayerView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setHorizontalSwipe(action: (Int) -> Unit) {
        controller?.let {
            if (it is TVPlayerControllerView) {
                it.setHorizontalSwipe(action)
            }
        }
    }

    override fun getControllerView(context: Context, attrs: AttributeSet?): PlayerControlView =
        TVPlayerControllerView(context, attrs)
}
