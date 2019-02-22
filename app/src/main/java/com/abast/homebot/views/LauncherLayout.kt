package com.abast.homebot.views

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Path
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.children
import kotlin.math.min
import kotlin.math.roundToInt


class LauncherLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val elementsMarginDp: Int = 20
    private val buttonsSizeDp: Int = 56
    private var elementsMargin: Int = dpToPixel(elementsMarginDp)
    private val ellipse: BottomCircleView = BottomCircleView(context)
    private val label: TextView = TextView(context).apply {
        text = "Test text very long u know"
        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        typeface = Typeface.DEFAULT_BOLD
        visibility = View.VISIBLE
    }
    private var isOnHold: Boolean = false

    init {
        addView(ellipse)
        addView(label)
        (1..7).map { QuickActionButton(context) }.forEach { button ->
            addView(button)
            button.setOnLongClickListener {
                spreadButtonsAround(button)
                isOnHold = true
                true
            }
            button.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if (isOnHold) {
                        resetButtonsPosition()
                        isOnHold = false
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
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
        resetButtonsPosition()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        elementsMargin = if (h > w) w / 20 else w / 10
        val cw = MeasureSpec.makeMeasureSpec(dpToPixel(buttonsSizeDp), MeasureSpec.EXACTLY)
        children.filter { it is QuickActionButton }.forEach {
            measureChild(it, cw, cw)
        }
        measureChild(ellipse, widthMeasureSpec, heightMeasureSpec)
        measureChild(
            label,
            MeasureSpec.makeMeasureSpec(w / 3, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        setMeasuredDimension(w, h)
    }

    private fun spreadButtonsAround(child: View) {
        var labelStartX = ((child.x + (child.measuredWidth / 2)) - (label.measuredWidth / 2)).roundToInt()
        if (labelStartX < elementsMargin) {
            labelStartX = elementsMargin
        } else if ((labelStartX + label.measuredWidth) > (measuredWidth - elementsMargin)) {
            labelStartX = (measuredWidth - elementsMargin) - label.measuredWidth
        }
        val labelStartY = (child.y - label.measuredHeight).roundToInt()
        Log.d("HB", "label w: ${label.measuredWidth}, h: ${label.measuredHeight}")
        label.layout(labelStartX, labelStartY, labelStartX + label.measuredWidth, labelStartY + label.measuredHeight)
        val buttons = children.filter { it is QuickActionButton }.map { it as QuickActionButton }.toList()
        val index = buttons.indexOf(child)
        val spaceBefore = (child.x - elementsMargin).roundToInt()
        val spaceAfter = ((measuredWidth - (child.x + child.measuredWidth)) - elementsMargin).roundToInt()
        var availableSpaceLeft = if (index > 0) (spaceBefore / index) else 0
        var availableSpaceRight = if (((buttons.size - index) - 1) > 0) spaceAfter / ((buttons.size - index) - 1) else 0
        val singleChildWidth = buttons.lastOrNull()?.measuredWidth ?: 0
        if (singleChildWidth > availableSpaceLeft && index > 1) {
            availableSpaceLeft = (spaceBefore - singleChildWidth) / (index - 1)
        }
        if (singleChildWidth > availableSpaceRight && ((buttons.size - index) - 1) > 1) {
            availableSpaceRight = (spaceAfter - singleChildWidth) / ((buttons.size - index) - 2)
        }
        (0 until index).map { buttons[it] }
            .fold(min(elementsMargin, (child.x - singleChildWidth).roundToInt())) { acc, view ->
                val startingX =
                    acc + if (availableSpaceLeft > view.measuredWidth) (availableSpaceLeft - view.measuredWidth) / 2 else 0
                val startingY =
                    ellipse.getPositiveY(startingX + (view.measuredWidth / 2) - (measuredWidth / 2)) - (view.measuredHeight / 2)
                ObjectAnimator.ofFloat(view, View.X, View.Y, Path().apply {
                    moveTo(view.x, view.y)
                    lineTo(startingX.toFloat(), startingY.toFloat())
                }).apply {
                    duration = 300
                    start()
                }
                acc + availableSpaceLeft
            }
        ((index + 1) until buttons.size).map { buttons[it] }
            .fold((child.x + child.measuredWidth).roundToInt()) { acc, view ->
                val startingX =
                    acc + if (availableSpaceRight > view.measuredWidth) (availableSpaceRight - view.measuredWidth) / 2 else 0
                val startingY =
                    ellipse.getPositiveY(startingX + (view.measuredWidth / 2) - (measuredWidth / 2)) - (view.measuredHeight / 2)
                ObjectAnimator.ofFloat(view, View.X, View.Y, Path().apply {
                    moveTo(view.x, view.y)
                    lineTo(startingX.toFloat(), startingY.toFloat())
                }).apply {
                    duration = 300
                    start()
                }
                acc + availableSpaceRight
            }
    }

    private fun resetButtonsPosition() {
        label.layout(0, 0, 0, 0)
        val buttons = children.filter { it is QuickActionButton }.toList()
        val availableWidth = (measuredWidth - (2 * elementsMargin))
        var availableSpace = availableWidth / buttons.size
        val singleChildWidth = children.lastOrNull()?.measuredWidth ?: 0
        if (singleChildWidth > availableSpace) {
            availableSpace = (availableWidth - singleChildWidth) / (buttons.size - 1)
        }
        buttons.fold(elementsMargin) { acc, view ->
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

    fun setOnButtonClickListener(listener: (QuickActionButton) -> Unit) {
        children.filter { it is QuickActionButton }.map { it as QuickActionButton }.forEach { button ->
            button.setOnClickListener { listener(button) }
        }
    }

    private fun dpToPixel(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).roundToInt()

}