
package com.hisab.kitab

import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView

object ScrollContainer {

    fun wrap(
        content: View,
        fillViewport: Boolean = true
    ): ScrollView {
        return ScrollView(content.context).apply {
            isFillViewport = fillViewport
            isVerticalScrollBarEnabled = true
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS

            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }
}
