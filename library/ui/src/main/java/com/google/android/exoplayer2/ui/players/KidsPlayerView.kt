package com.google.android.exoplayer2.ui.players

import android.content.Context
import android.util.AttributeSet
import com.google.android.exoplayer2.ui.KidsPlayerControllerView
import com.google.android.exoplayer2.ui.PlayerControlView

/**
 * Created by André Carvalho on 15/10/2019
 */
class KidsPlayerView : PlayerView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun getControllerView(context: Context, attrs: AttributeSet?): PlayerControlView =
        KidsPlayerControllerView(context, attrs)
}
