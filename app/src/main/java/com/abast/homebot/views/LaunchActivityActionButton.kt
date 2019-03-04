package com.abast.homebot.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.util.AttributeSet
import com.abast.homebot.R
import java.net.URISyntaxException
import kotlin.math.roundToInt

class LaunchActivityActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : QuickActionButton(context, attrs, defStyleAttr, defStyleRes) {
    private var uri: String?

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.LaunchActivityActionButton, defStyleAttr, defStyleRes)
            .apply {
                try {
                    uri = getString(R.styleable.LaunchActivityActionButton_targetPackage)
                } finally {
                    recycle()
                }
            }
        setOnClickListener {
            val activity = context as Activity
            if (uri != null) {
                try {
                    val shortcut = Intent.parseUri(uri, 0).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    activity.finish()
                    activity.startActivity(shortcut)
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                    launchMainActivity()
                }
            }
        }
    }

    fun setPackageName(name: String) {
        uri = name
    }

    override fun getLabel(): String =
        context.packageManager.resolveActivity(Intent.parseUri(uri, 0), 0).activityInfo.loadLabel(context.packageManager).toString()

    override fun onDraw(canvas: Canvas) {
        context.packageManager.getActivityIcon(Intent.parseUri(uri, 0)).apply {
            setBounds(
                (centerX - radius).roundToInt(),
                (centerY - radius).roundToInt(),
                (centerX + radius).roundToInt(),
                (centerY + radius).roundToInt()
            )
        }.draw(canvas)
    }

}