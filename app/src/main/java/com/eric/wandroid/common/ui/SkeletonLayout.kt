package com.eric.wandroid.common.ui

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

/** A lightweight pulsing container for placeholder content. */
class SkeletonLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val pulseAnimator = ObjectAnimator.ofFloat(this, View.ALPHA, MIN_ALPHA, MAX_ALPHA).apply {
        duration = PULSE_DURATION_MS
        repeatMode = ObjectAnimator.REVERSE
        repeatCount = ObjectAnimator.INFINITE
        interpolator = FastOutSlowInInterpolator()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateAnimation()
    }

    override fun onDetachedFromWindow() {
        pulseAnimator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        updateAnimation()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        updateAnimation()
    }

    private fun updateAnimation() {
        if (!isAttachedToWindow) return

        if (visibility == VISIBLE && windowVisibility == VISIBLE) {
            if (!pulseAnimator.isStarted) {
                alpha = MAX_ALPHA
                pulseAnimator.start()
            }
        } else {
            pulseAnimator.cancel()
            alpha = MAX_ALPHA
        }
    }

    private companion object {
        const val MIN_ALPHA = 0.58f
        const val MAX_ALPHA = 1f
        const val PULSE_DURATION_MS = 850L
    }
}
