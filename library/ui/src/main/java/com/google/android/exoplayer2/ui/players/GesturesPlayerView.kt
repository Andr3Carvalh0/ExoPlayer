package com.google.android.exoplayer2.ui.players

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.google.android.exoplayer2.ui.R
import com.google.android.exoplayer2.ui.views.AspectRatioFrameLayout.RESIZE_MODE_FIT
import com.google.android.exoplayer2.ui.views.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import android.view.animation.Animation
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import com.google.android.exoplayer2.ui.utilities.isTablet
import com.google.android.exoplayer2.ui.utilities.isTallDevice
import com.google.android.exoplayer2.ui.utilities.px
import com.google.android.exoplayer2.ui.utilities.screenWidth
import com.google.android.exoplayer2.ui.utilities.navigation
import com.google.android.exoplayer2.ui.utilities.statusbar

/**
 * Created by André Carvalho on 2019-12-30
 */
class GesturesPlayerView : PlayerView {

    companion object {
        const val SWIPE_X_THRESHOLD = 200
        const val SWIPE_Y_THRESHOLD = 100
        const val SWIPE_VELOCITY_THRESHOLD = 100
        const val SWIPE_X_START_MARGIN_DP = 24
        const val PINCH_COOLDOWN = 500L
        const val PINCH_MIN_FACTOR_FIT = 0.97f
        const val PINCH_MIN_FACTOR_ZOOM = 0.8f
        const val PINCH_MAX_FACTOR_FIT = 1.23f
        const val PINCH_MAX_FACTOR_ZOOM = 1.02f
        const val PINCH_MIN_TO_ZOOM_THRESHOLD = 1.1f
        const val PINCH_MIN_TO_FIT_THRESHOLD = 0.95f
        const val PINCH_DEFAULT_FACTOR = 1.0f
        const val EDGES_FADE_OUT = 150L
        const val EDGES_FADE_IN = 75L
        const val MAX_ALPHA_WHEN_ZOOMING = 0.85f
        const val FADE_IN_ZOOM = 350L
        const val ON_SCREEN_ZOOM_MESSAGE = 1000L
    }

    private val swipeGestureDetector: GestureDetectorCompat
    private val tapGestureDetector: GestureDetectorCompat
    private val pinchGestureDetector: ScaleGestureDetector
    private var handleHorizontalSwipe: (Int) -> Unit = {}
    private var handleSwipeUp: () -> Unit = {}
    private var onResizePlayer: (Int) -> Unit = {}
    private var lastPinchTimestamp: Long = 0L

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        swipeGestureDetector = GestureDetectorCompat(context, SwipeGestureListener())
        tapGestureDetector = GestureDetectorCompat(context, TapGestureListener())
        pinchGestureDetector = ScaleGestureDetector(context, PinchGestureListener())
    }

    fun setHorizontalSwipe(action: (Int) -> Unit) { this.handleHorizontalSwipe = action }
    fun setSwipeUp(action: () -> Unit) { this.handleSwipeUp = action }
    fun setOnResizePlayer(action: (Int) -> Unit) { this.onResizePlayer = action }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val isOutOfTimeout = abs(lastPinchTimestamp - System.currentTimeMillis()) > PINCH_COOLDOWN

        if (tapGestureDetector.onTouchEvent(event)) { if (controller?.ignoreTouch() == true || !useController) { return true } }
        if (!pinchGestureDetector.isInProgress && isOutOfTimeout && useController) { if (swipeGestureDetector.onTouchEvent(event)) { return true } }
        if (!context.isTablet && context.isTallDevice) { pinchGestureDetector.onTouchEvent(event) }

        return if (isOutOfTimeout) super.onTouchEvent(event) else true
    }

    private inner class SwipeGestureListener : GestureDetector.OnGestureListener {

        override fun onShowPress(e: MotionEvent?) { }
        override fun onSingleTapUp(e: MotionEvent?): Boolean = false
        override fun onDown(e: MotionEvent?): Boolean = false
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean = false
        override fun onLongPress(e: MotionEvent?) { }

        override fun onFling(
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffY: Float = (moveEvent?.y ?: 1f) - (downEvent?.y ?: 1f)
            val diffX: Float = (moveEvent?.x ?: 1f) - (downEvent?.x ?: 1f)

            return if (abs(diffX) <= abs(diffY)) {
                if (abs(diffY) > SWIPE_Y_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) onSwipeDown() else onSwipeUp()
                    true
                } else false
            } else {
                if (abs(diffX) > SWIPE_X_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (controller?.ignoreTouch() != true && !pinchGestureDetector.isInProgress) {
                        if ((downEvent?.rawX ?: 0f) <= SWIPE_X_START_MARGIN_DP.px ||
                            (downEvent?.rawX ?: 0f) > ((context.screenWidth()?.toFloat() ?: 0f) + context.navigation() + context.statusbar()) - SWIPE_X_START_MARGIN_DP.px
                        ) {
                            return false
                        }

                        onSwipeDown()
                        handleHorizontalSwipe.invoke(if (diffX > 0) -1 else 1)
                        true
                    } else false
                } else false
            }
        }

        private fun onSwipeUp() {
            handleSwipeUp.invoke()
            controller?.onSwipeUp()
        }

        private fun onSwipeDown() { controller?.onSwipeDown() }
    }
    private inner class TapGestureListener : GestureDetector.OnGestureListener {

        override fun onShowPress(e: MotionEvent?) { }
        override fun onSingleTapUp(e: MotionEvent?): Boolean = true
        override fun onDown(e: MotionEvent?): Boolean = false
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean = false
        override fun onLongPress(e: MotionEvent?) { }
        override fun onFling(
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean = false
    }
    private inner class PinchGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        var currentFactor = 1.0f

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            controller?.forceHide()
            lastPinchTimestamp = System.currentTimeMillis()
            edges?.alpha = 0f
            return super.onScaleBegin(detector)
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            super.onScaleEnd(detector)

            val zoomToFit = currentFactor >= PINCH_MIN_TO_ZOOM_THRESHOLD && (contentFrame?.resizeMode ?: RESIZE_MODE_ZOOM) != RESIZE_MODE_ZOOM
            val fit = currentFactor <= PINCH_MIN_TO_FIT_THRESHOLD && (contentFrame?.resizeMode ?: RESIZE_MODE_FIT) != RESIZE_MODE_FIT

            if (zoomToFit || fit) {
                contentFrame?.resizeMode = if (zoomToFit) RESIZE_MODE_ZOOM else RESIZE_MODE_FIT
            } else {
                // We are in between states, so we will fallback to the closest one
                contentFrame?.resizeMode = if (abs(PINCH_MIN_TO_ZOOM_THRESHOLD - currentFactor) <= abs(PINCH_MIN_TO_FIT_THRESHOLD - currentFactor)) RESIZE_MODE_ZOOM else RESIZE_MODE_FIT
            }

            currentFactor = PINCH_DEFAULT_FACTOR
            onResizePlayer.invoke(contentFrame?.resizeMode ?: RESIZE_MODE_FIT)
            contentFrame?.scaleX = currentFactor
            contentFrame?.scaleY = currentFactor

            lastPinchTimestamp = System.currentTimeMillis()

            if ((contentFrame?.resizeMode ?: RESIZE_MODE_FIT) == RESIZE_MODE_ZOOM) {
                edges?.let {
                    it.animation = AlphaAnimation(it.alpha, 1.0f).apply {
                        interpolator = DecelerateInterpolator()
                        fillAfter = true
                        fillBefore = true
                        duration = EDGES_FADE_IN
                        setAnimationListener(
                            object : Animation.AnimationListener {
                                override fun onAnimationRepeat(animation: Animation?) { }
                                override fun onAnimationEnd(animation: Animation?) {
                                    it.animation = AlphaAnimation(1.0f, 0.0f).apply {
                                        interpolator = AccelerateInterpolator()
                                        fillAfter = true
                                        fillBefore = true
                                        duration = EDGES_FADE_OUT
                                    }

                                    it.animate().start()
                                }
                                override fun onAnimationStart(animation: Animation?) { }
                            }
                        )
                    }

                    it.animate().start()
                }
            }

            zoomMessages?.let {
                it.text = context.getString(if ((contentFrame?.resizeMode ?: RESIZE_MODE_FIT) == RESIZE_MODE_ZOOM) R.string.fullscreen else R.string.original)

                it.animation = AlphaAnimation(0.0f, 1.0f).apply {
                    interpolator = DecelerateInterpolator()
                    fillAfter = true
                    fillBefore = true
                    duration = EDGES_FADE_IN
                    setAnimationListener(
                        object : Animation.AnimationListener {
                            override fun onAnimationRepeat(animation: Animation?) { }
                            override fun onAnimationEnd(animation: Animation?) {
                                it.animation = AlphaAnimation(1.0f, 0.0f).apply {
                                    interpolator = AccelerateInterpolator()
                                    startOffset = ON_SCREEN_ZOOM_MESSAGE
                                    fillAfter = true
                                    fillBefore = true
                                    duration = FADE_IN_ZOOM
                                }

                                it.animate().start()
                            }
                            override fun onAnimationStart(animation: Animation?) { it.visibility = View.VISIBLE }
                        }
                    )
                }

                it.animate().start()
            }

            controller?.forceHide()
        }

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            if (detector == null || controller?.ignoreTouch() == true) return false

            controller?.forceHide()
            currentFactor += detector.scaleFactor - 1f

            val minScale = if (contentFrame?.resizeMode ?: RESIZE_MODE_ZOOM == RESIZE_MODE_ZOOM) PINCH_MIN_FACTOR_ZOOM else PINCH_MIN_FACTOR_FIT
            val maxScale = if (contentFrame?.resizeMode ?: RESIZE_MODE_ZOOM == RESIZE_MODE_ZOOM) PINCH_MAX_FACTOR_ZOOM else PINCH_MAX_FACTOR_FIT

            val normalizedFactor = min(max(currentFactor, minScale), maxScale)

            contentFrame?.scaleX = normalizedFactor
            contentFrame?.scaleY = normalizedFactor

            if (contentFrame?.resizeMode ?: RESIZE_MODE_ZOOM == RESIZE_MODE_FIT && normalizedFactor >= PINCH_DEFAULT_FACTOR) {
                edges?.alpha = min(MAX_ALPHA_WHEN_ZOOMING, (1.0f - (PINCH_MAX_FACTOR_FIT - normalizedFactor) / (PINCH_MAX_FACTOR_FIT - PINCH_DEFAULT_FACTOR)))
            }

            lastPinchTimestamp = System.currentTimeMillis()
            return true
        }
    }
}
