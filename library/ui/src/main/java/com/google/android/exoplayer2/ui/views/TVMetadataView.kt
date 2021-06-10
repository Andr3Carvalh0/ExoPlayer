package com.google.android.exoplayer2.ui.views

import android.content.Context
import android.util.AttributeSet
import com.google.android.exoplayer2.ui.R

/**
 * Created by André Carvalho on 2019-10-30
 */
class TVMetadataView : MetadataView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun layout(): Int = R.layout.tv_new_program_info_view

    override fun liveTvTextSize(): Float = 19f

    override fun vodTvTextSize(): Float = 24f
}
