package com.abast.homebot.views

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.children
import kotlin.math.roundToInt


class LauncherLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val elementsMarginDp: Int = 20
    private val buttonsSizeDp: Int = 56
    private var elementsMargin: Int = dpToPixel(elementsMarginDp)
    private val ellipse: BottomCircleView = BottomCircleView(context)
    private val label: TextView = TextView(context).apply {
        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        setTextColor(Color.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        visibility = View.VISIBLE
    }
    private var spreadingAround: View? = null
    private var childrenRadius: Int = 0
    private var childrenPositions: List<Pair<Int, Int>> = emptyList()

    init {
        setButtons(emptyList())
    }

    fun setButtons(buttons: List<QuickActionButton>) {
        removeAllViews()
        addView(ellipse)
        addView(label)
        buttons.forEach { button ->
            addView(button)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        fun triggerSpread() {
            buttons().zip(childrenPositions.indices).filter { it.first != spreadingAround }.find { (_, index) ->
                val position = childrenPositions[index]
                (event.x >= position.first && event.x <= (position.first + childrenRadius * 2)) && (
                        childrenPositions.lastIndex == index || event.x <= childrenPositions[index + 1].first
                        )
            }?.let { (button, _) ->
                spreadingAround = button
                label.text = button.getLabel()
                invalidate()
                requestLayout()
            }
        }

        fun triggerIfInArea() {
            val rect = Rect()
            if (ellipse.ellipseContains(event.x.roundToInt(), event.y.roundToInt()) ||
                buttons().any {
                    it.getGlobalVisibleRect(rect)
                    rect.contains(event.x.toInt(), event.y.toInt())
                }
            ) {
                triggerSpread()
            } else if (spreadingAround != null) {
                spreadingAround = null
                invalidate()
                requestLayout()
            }
        }
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                val around = spreadingAround
                if (around != null) {
                    around.performClick()
                } else {
                    spreadingAround = null
                    invalidate()
                    requestLayout()
                }
            }
            MotionEvent.ACTION_DOWN -> if (spreadingAround == null) {
                triggerIfInArea()
            }
            MotionEvent.ACTION_MOVE -> triggerIfInArea()
        }
        super.onTouchEvent(event)
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        ellipse.layout(0, 0, measuredWidth, measuredHeight)
        val buttons = buttons()
        childrenRadius = buttons.first().measuredWidth / 2
        val availableWidth = (measuredWidth - (2 * elementsMargin))
        var availableSpace = availableWidth / buttons.size
        val singleChildWidth = children.lastOrNull()?.measuredWidth ?: 0
        if (singleChildWidth > availableSpace) {
            availableSpace = (availableWidth - singleChildWidth) / (buttons.size - 1)
        }
        val positions: MutableList<Pair<Int, Int>> = mutableListOf()
        buttons.fold(elementsMargin) { acc, view ->
            val startingX =
                acc + if (availableSpace > view.measuredWidth) (availableSpace - view.measuredWidth) / 2 else 0
            val startingY =
                ellipse.getPositiveY(startingX + (view.measuredWidth / 2) - (measuredWidth / 2)) - (view.measuredHeight / 2)
            positions.add(Pair(startingX, startingY))
            acc + availableSpace
        }
        childrenPositions = positions
        val centerX = (measuredWidth / 2)
        val centerY = ellipse.getPositiveY(centerX - (measuredWidth / 2))
        buttons.forEach { button ->
            button.layout(
                centerX - childrenRadius,
                centerY - childrenRadius,
                centerX + childrenRadius,
                centerY + childrenRadius
            )
        }
        val holdedButton = spreadingAround
        if (buttons.count() > 0) {
            if (holdedButton == null) {
                resetButtonsPosition()
            } else {
                spreadButtonsAround(holdedButton)
            }
        }
        Log.d("HB", "onLayout")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        elementsMargin = if (h > w) w / 20 else w / 10
        val cw = MeasureSpec.makeMeasureSpec(dpToPixel(buttonsSizeDp), MeasureSpec.EXACTLY)
        buttons().forEach {
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
        val buttons = buttons()
        val index = buttons.indexOf(child)
        val childPos = childrenPositions[index]
        var labelStartX = ((childPos.first + (child.measuredWidth / 2)) - (label.measuredWidth / 2))
        if (labelStartX < elementsMargin) {
            labelStartX = elementsMargin
        } else if ((labelStartX + label.measuredWidth) > (measuredWidth - elementsMargin)) {
            labelStartX = (measuredWidth - elementsMargin) - label.measuredWidth
        }
        val labelStartY = (childPos.second - label.measuredHeight)
        label.layout(labelStartX, labelStartY, labelStartX + label.measuredWidth, labelStartY + label.measuredHeight)
        val singleChildWidth = buttons.lastOrNull()?.measuredWidth ?: 0
        val spaceBefore = (childPos.first - elementsMargin)
        val spaceAfter = ((measuredWidth - (childPos.first + child.measuredWidth)) - elementsMargin)
        var availableSpaceLeftPerButton = if (index > 0) (spaceBefore / index) else 0
        var availableSpaceRightPerButton =
            if (((buttons.size - index) - 1) > 0) spaceAfter / ((buttons.size - index) - 1) else 0
        if (singleChildWidth > availableSpaceLeftPerButton && index > 1 && spaceBefore > singleChildWidth) {
            availableSpaceLeftPerButton = (spaceBefore - singleChildWidth) / (index - 1)
        }
        if (singleChildWidth > availableSpaceRightPerButton && ((buttons.size - index) - 1) > 1 && spaceAfter > singleChildWidth) {
            availableSpaceRightPerButton = (spaceAfter - singleChildWidth) / ((buttons.size - index) - 2)
        }
        ObjectAnimator.ofFloat(child, View.X, View.Y, Path().apply {
            moveTo(child.x, child.y)
            lineTo(childPos.first.toFloat(), childPos.second.toFloat())
        }).apply {
            duration = 300
            start()
        }
        (0 until index).reversed().map { Pair(buttons[it], it) }
            .fold(childPos.first) { acc, (view, pos) ->
                val startingX =
                    (acc - if (availableSpaceLeftPerButton > view.measuredWidth) (availableSpaceLeftPerButton - view.measuredWidth) / 2 else 0) -
                            view.measuredWidth
                if (startingX <= childrenPositions[pos].first) {
                    val startingY =
                        ellipse.getPositiveY(startingX + (view.measuredWidth / 2) - (measuredWidth / 2)) - (view.measuredHeight / 2)
                    ObjectAnimator.ofFloat(view, View.X, View.Y, Path().apply {
                        moveTo(view.x, view.y)
                        lineTo(startingX.toFloat(), startingY.toFloat())
                    }).apply {
                        duration = 300
                        start()
                    }
                }
                acc - availableSpaceLeftPerButton
            }
        ((index + 1) until buttons.size).map { Pair(buttons[it], it) }
            .fold(childPos.first + child.measuredWidth) { acc, (view, pos) ->
                val startingX =
                    acc + if (availableSpaceRightPerButton > view.measuredWidth) (availableSpaceRightPerButton - view.measuredWidth) / 2 else 0
                if (startingX >= childrenPositions[pos].first) {
                    val startingY =
                        ellipse.getPositiveY(startingX + (view.measuredWidth / 2) - (measuredWidth / 2)) - (view.measuredHeight / 2)
                    ObjectAnimator.ofFloat(view, View.X, View.Y, Path().apply {
                        moveTo(view.x, view.y)
                        lineTo(startingX.toFloat(), startingY.toFloat())
                    }).apply {
                        duration = 300
                        start()
                    }
                }
                acc + availableSpaceRightPerButton
            }
    }

    private fun buttons(): List<QuickActionButton> =
        children.filter { it is QuickActionButton }.map { it as QuickActionButton }.toList()

    private fun resetButtonsPosition() {
        label.layout(0, 0, 0, 0)
        buttons().zip(childrenPositions).forEach { (view, p) ->
            ObjectAnimator.ofFloat(view, View.X, View.Y, Path().apply {
                moveTo(view.x, view.y)
                lineTo(p.first.toFloat(), p.second.toFloat())
            }).apply {
                duration = 300
                start()
            }
        }
    }

    private fun dpToPixel(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).roundToInt()

}