package com.google.android.exoplayer2.ui

import android.content.Context
import android.util.AttributeSet

class KidsPlayerControllerView : PlayerControlView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun layoutID(): Int = R.layout.exo_player_kids_control_view

    override fun showRemainingTime(): Boolean = true
}
