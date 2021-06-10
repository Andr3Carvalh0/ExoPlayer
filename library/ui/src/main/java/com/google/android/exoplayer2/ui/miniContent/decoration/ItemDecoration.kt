package com.google.android.exoplayer2.ui.miniContent.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ui.utilities.px

/**
 * Created by André Carvalho on 2019-12-31
 */
class ItemDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        val totalItems = parent.adapter!!.itemCount

        outRect.left = 16.px

        if (position + 1 >= totalItems) {
            outRect.right = 16.px
        }
    }
}
