package com.google.android.exoplayer2.ui.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ui.R
import com.google.android.exoplayer2.ui.utilities.SpannableUtilities

/**
 * Created by André Carvalho on 2019-10-30
 */
open class MetadataView : ConstraintLayout {

    private lateinit var channelContainer: LinearLayoutCompat
    private lateinit var channelLogo: AppCompatImageView
    private lateinit var channelName: AppCompatTextView
    private lateinit var programTitle: AppCompatTextView
    private lateinit var programDuration: AppCompatTextView

    open fun layout(): Int = R.layout.new_program_info_view

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initialize()
    }

    private fun initialize() {
        val rootView = View.inflate(context, layout(), this)

        channelContainer = rootView.findViewById(R.id.channelContainer)
        channelLogo = rootView.findViewById(R.id.channelLogo)
        channelName = rootView.findViewById(R.id.channelName)
        programTitle = rootView.findViewById(R.id.title)
        programDuration = rootView.findViewById(R.id.subtitle)
    }

    fun logo(): AppCompatImageView = channelLogo

    fun metadata(
        image: String,
        channelLogo: String,
        channelName: String,
        programTitle: String,
        programDuration: String
    ) {
        Glide.with(context).load(image).into(this.channelLogo)

        this.channelName.text = channelName

        this.channelContainer.visibility = if (channelLogo.isEmpty()) View.GONE else View.VISIBLE
        this.channelName.visibility = if (channelLogo.isEmpty()) View.GONE else View.VISIBLE

        this.programTitle.text = programTitle
        this.programDuration.text = SpannableUtilities.twoTone(
            R.color.light_grey,
            R.color.silver,
            "|",
            programDuration,
            context
        )

        this.programTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (channelLogo.isEmpty()) liveTvTextSize() else vodTvTextSize())
    }

    open fun liveTvTextSize(): Float = 19f

    open fun vodTvTextSize(): Float = 19f
}
