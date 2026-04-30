package com.eric.wandroid.common.ui

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

object ScrollToTopHelper {
    fun attach(
        recyclerView: RecyclerView,
        button: FloatingActionButton,
        forceHide: () -> Boolean = { false }
    ) {
        placeAtBottomEnd(button)
        button.hide()
        button.setOnClickListener {
            scrollToTop(recyclerView)
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                update(recyclerView, button, forceHide())
            }
        })
        recyclerView.post {
            update(recyclerView, button, forceHide())
        }
    }

    fun update(
        recyclerView: RecyclerView,
        button: FloatingActionButton,
        forceHide: Boolean
    ) {
        val shouldShow = !forceHide &&
            recyclerView.computeVerticalScrollOffset() > recyclerView.height
        if (shouldShow) {
            button.show()
        } else {
            button.hide()
        }
    }

    private fun scrollToTop(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
        val itemCount = recyclerView.adapter?.itemCount ?: 0
        val firstVisible = layoutManager?.findFirstVisibleItemPosition() ?: RecyclerView.NO_POSITION
        recyclerView.stopScroll()
        when {
            itemCount <= 1 -> layoutManager?.scrollToPositionWithOffset(0, 0)
            firstVisible > 12 && layoutManager != null -> {
                val anchorPosition = minOf(6, itemCount - 1)
                layoutManager.scrollToPositionWithOffset(anchorPosition, 0)
                recyclerView.post { recyclerView.smoothScrollToPosition(0) }
            }

            layoutManager != null -> recyclerView.smoothScrollToPosition(0)
            else -> recyclerView.scrollToPosition(0)
        }
    }

    private fun placeAtBottomEnd(button: FloatingActionButton) {
        val parent = button.parent as? ConstraintLayout ?: return
        val constraintSet = ConstraintSet()
        constraintSet.clone(parent)
        constraintSet.clear(button.id)
        constraintSet.constrainWidth(button.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(button.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.connect(button.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, dp(button, 16))
        constraintSet.connect(button.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, dp(button, 16))
        constraintSet.applyTo(parent)
        button.bringToFront()
    }

private fun dp(view: View, value: Int): Int {
        return (value * view.resources.displayMetrics.density).toInt()
    }
}
