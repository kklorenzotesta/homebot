package com.abast.homebot.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class BottomCircleView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var mRectF: RectF = RectF()
    private var bottomRect: RectF = RectF()

    private val circlePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#66000000")
            style = Paint.Style.FILL
            setShadowLayer(55f, 0f, 0f, Color.BLACK)
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val minH = min(w, h) / if (h > w) 5F else 4F
        if (h > w) {
            val navBarH = getNavBarHeight().toFloat()
            mRectF = RectF(-w / 4F, (h - minH) - navBarH, w * 5F / 4F, (h + minH) - navBarH)
            bottomRect = RectF(0F, h - navBarH, w.toFloat(), h.toFloat())
        } else {
            mRectF = RectF(-w / 3F, h - minH, w * 4F / 3F, h + minH)
            bottomRect = RectF()
        }
    }

    private fun getNavBarHeight(): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawArc(mRectF, 180F, 180F, true, circlePaint)
        canvas.drawRect(bottomRect, circlePaint)
    }

    fun getPositiveY(x: Int): Int =
            /*((mRectF.height() / 2.0) * sqrt(1 - (x / (mRectF.width() / 2.0).pow(2)))).roundToInt()*/
        (((mRectF.height() / 2.0) - (((mRectF.height() / 2.0) / (mRectF.width() / 2.0)) * sqrt(
            (mRectF.width() / 2.0).pow(2) - x.toDouble().pow(
                2
            )
        ))) + mRectF.top).roundToInt()
            .also {
                Log.d("HB", "measured from $x -> $it")
            }
    // + mRectF.top.roundToInt()
    //b*sqrt(1-(x./a)^2)
}