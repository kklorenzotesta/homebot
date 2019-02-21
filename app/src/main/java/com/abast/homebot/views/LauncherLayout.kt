package com.abast.homebot.views

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.children
import kotlin.math.roundToInt


class LauncherLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val elementsMarginDp: Int = 20
    private val buttonsSizeDp: Int = 56
    private var elementsMargin: Int = dpToPixel(elementsMarginDp)
    private val ellipse: BottomCircleView = BottomCircleView(context)

    init {
        addView(ellipse)
        (1..7).map { QuickActionButton(context) }.forEach {
            addView(it)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        ellipse.layout(0, 0, measuredWidth, measuredHeight)
        val singleChildRadius = (children.lastOrNull()?.measuredWidth ?: 0) / 2
        val centerX = (measuredWidth / 2)
        val centerY = ellipse.getPositiveY(centerX - (measuredWidth / 2))
        children.filter { it is QuickActionButton }.forEach { button ->
            button.layout(
                centerX - singleChildRadius,
                centerY - singleChildRadius,
                centerX + singleChildRadius,
                centerY + singleChildRadius
            )
        }
        spreadButtons()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        elementsMargin = if (h > w) w / 20 else w / 10
        val cw = MeasureSpec.makeMeasureSpec(dpToPixel(buttonsSizeDp), MeasureSpec.EXACTLY)
        children.filter { it is QuickActionButton }.forEach {
            measureChild(it, cw, cw)
        }
        setMeasuredDimension(w, h)
    }

    private fun spreadButtons() {
        val availableWidth = (measuredWidth - (2 * elementsMargin))
        var availableSpace = availableWidth / (childCount - 1)
        val singleChildWidth = children.lastOrNull()?.measuredWidth ?: 0
        if (singleChildWidth > availableSpace) {
            availableSpace = (availableWidth - singleChildWidth) / (childCount - 2)
        }
        children.filter { it is QuickActionButton }.fold(elementsMargin) { acc, view ->
            val startingX =
                acc + if (availableSpace > view.measuredWidth) (availableSpace - view.measuredWidth) / 2 else 0
            val startingY =
                ellipse.getPositiveY(startingX + (view.measuredWidth / 2) - (measuredWidth / 2)) - (view.measuredHeight / 2)
            ObjectAnimator.ofFloat(view, View.X, View.Y, Path().apply {
                moveTo(view.x, view.y)
                lineTo(startingX.toFloat(), startingY.toFloat())
            }).apply {
                duration = 300
                start()
            }
            acc + availableSpace
        }
    }

    private fun dpToPixel(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).roundToInt()

}