package com.google.android.exoplayer2.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ui.views.TVPlaybackButton
import java.util.LinkedList

class TVPlayerControllerView : PlayerControlView {

    private val next: TVPlaybackButton
    private val previous: TVPlaybackButton
    private val miniepg: TVPlaybackButton

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        next = findViewById<TVPlaybackButton>(R.id.nexta).apply {
            setOnClickListener { handleHorizontalSwipe?.invoke(1) }
        }

        previous = findViewById<TVPlaybackButton>(R.id.previousa).apply {
            setOnClickListener { handleHorizontalSwipe?.invoke(-1) }
        }

        miniepg = findViewById<TVPlaybackButton>(R.id.epg).apply {
            setOnClickListener { toggleContentSheet({}) }
        }
    }

    override fun setMiniContentList(
        adapter: RecyclerView.Adapter<*>,
        scrollPosition: Int,
        isCircular: Boolean
    ) {
        super.setMiniContentList(adapter, scrollPosition, isCircular)
        miniepg.visibility = View.VISIBLE
    }

    override fun carouselItems(): LinkedList<View> {
        return super.carouselItems().apply {
            addAll(listOf(next, previous, miniepg))
        }
    }

    override fun changeBottomMargin(pixels: Int) { }

    override fun hideMiniContent() {
        super.hideMiniContent()
        miniepg.visibility = View.GONE
    }

    private var handleHorizontalSwipe: ((Int) -> Unit)? = null

    fun setHorizontalSwipe(action: (Int) -> Unit) {
        this.handleHorizontalSwipe = action
    }

    override fun layoutID(): Int = R.layout.exo_player_tv_control_view

    override fun isVisible(): Boolean = visibility == View.VISIBLE
}
