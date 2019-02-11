package com.abast.homebot.views

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View

class BottomCircleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val metrics = DisplayMetrics()
    private var centerX: Float = 0F
    private var centerY: Float = 0F
    private var radius: Float = 0F

    private val circlePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#66000000")
            style = Paint.Style.FILL
            setShadowLayer(55f, 0f, 0f, Color.BLACK)
        }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        display.getRealMetrics(metrics)
        centerX = metrics.widthPixels / 2.0F
        centerY = metrics.heightPixels.toFloat() + (metrics.widthPixels / 2F)
        radius = metrics.widthPixels.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(
            centerX,
            centerY,
            radius,
            circlePaint
        )
    }
}