package com.abast.homebot.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.abast.homebot.actions.HomeAction
import kotlin.math.min
import kotlin.math.roundToInt

class QuickActionButton : View {
    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        setOnClickListener {
            action?.run(context)
        }
    }

    private val shadowSizeDp: Int = 10

    var centerX: Float = 0F
    var centerY: Float = 0F
    var radius: Float = 0F

    private var action: HomeAction? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec) + dpToPixel(shadowSizeDp * 2),
            MeasureSpec.getSize(heightMeasureSpec) + dpToPixel(shadowSizeDp * 2)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2F
        centerY = h / 2F
        radius = (min(w, h) / 2F) - dpToPixel(shadowSizeDp)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        action?.let { action ->
            action.icon(context).apply {
                setBounds(
                    (centerX - radius).roundToInt(),
                    (centerY - radius).roundToInt(),
                    (centerX + radius).roundToInt(),
                    (centerY + radius).roundToInt()
                )
                draw(canvas)
            }
        }
    }

    fun setAction(action: HomeAction) {
        this.action = action
        invalidate()
        requestLayout()
    }

    fun getLabel(): String = action?.label(context) ?: ""

    private fun dpToPixel(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).roundToInt()

}