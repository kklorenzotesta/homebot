package com.abast.homebot.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.abast.homebot.MainActivity
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

abstract class QuickActionButton : View {
    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    private val shadowSizeDp: Int = 10

    protected var centerX: Float = 0F
    protected var centerY: Float = 0F
    protected var radius: Float = 0F

    private val circlePaint: Paint = Random(Random.nextInt()).let { _ ->
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            setARGB(255, 0, 0, 0)
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

    /**
     * Launches MainActivity. Used as fallback for any errors that might occur.
     */
    protected fun launchMainActivity() {
        val i = Intent(context, MainActivity::class.java)
        val activity = context as Activity
        activity.finish()
        activity.startActivity(i)
    }

    abstract fun getLabel(): String

    private fun dpToPixel(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).roundToInt()

}