package com.abast.homebot.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import androidx.core.view.children
import kotlin.math.roundToInt


class LauncherLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val elementsMarginDp: Int = 20
    private val buttonsSizeDp: Int = 56
    private val elementsMargin: Int by lazy {
        dpToPixel(elementsMarginDp)
    }

    init {
        addView(BottomCircleView(context))
        (1..7).map { QuickActionButton(context) }.forEach {
            addView(it)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val availableWidth = (measuredWidth - (2 * elementsMargin))
        var availableSpace = availableWidth / (childCount - 1)
        val singleChildWidth = children.lastOrNull()?.measuredWidth ?: 0
        if (singleChildWidth > availableSpace) {
            availableSpace = (availableWidth - singleChildWidth) / (childCount - 2)
        }
        val ellipse = children.find { it is BottomCircleView }!! as BottomCircleView
        ellipse.layout(0, 0, measuredWidth, measuredHeight)
        children.filter { it is QuickActionButton }.fold(elementsMargin) { acc, view ->
            val startingX =
                acc + if (availableSpace > view.measuredWidth) (availableSpace - view.measuredWidth) / 2 else 0
            val startingY = ellipse.getPositiveY(startingX + (view.measuredWidth / 2) - (measuredWidth / 2)) - (view.measuredHeight / 2)
            view.layout(
                startingX,
                startingY,
                startingX + view.measuredWidth,
                startingY + view.measuredHeight
            )
            acc + availableSpace
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val cw = MeasureSpec.makeMeasureSpec(dpToPixel(buttonsSizeDp), MeasureSpec.EXACTLY)
        children.filter { it is QuickActionButton }.forEach {
            measureChild(it, cw, cw)
        }
        setMeasuredDimension(w, h)
    }

    private fun dpToPixel(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).roundToInt()

}