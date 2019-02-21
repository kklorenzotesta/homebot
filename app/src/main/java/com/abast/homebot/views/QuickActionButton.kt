package com.abast.homebot.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

class QuickActionButton(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val shadowSizeDp: Int = 10

    private var centerX: Float = 0F
    private var centerY: Float = 0F
    private var radius: Float = 0F

    private val circlePaint: Paint = Random(Random.nextInt()).let { rnd ->
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            style = Paint.Style.FILL
            setShadowLayer(shadowSizeDp.toFloat(), 0f, 0f, Color.BLACK)
        }
    }

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
        canvas.drawCircle(centerX, centerY, radius, circlePaint)
    }

    private fun dpToPixel(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).roundToInt()

}