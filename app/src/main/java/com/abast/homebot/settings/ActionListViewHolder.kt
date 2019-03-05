package com.abast.homebot.settings

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.abast.homebot.R
import com.abast.homebot.actions.HomeAction


class ActionListViewHolder(view: View, private val touchHelper: ItemTouchHelper) : RecyclerView.ViewHolder(view) {
    private var lastAction: HomeAction? = null

    private val label: TextView = view.findViewById(R.id.action_label)
    private val icon: ImageView = view.findViewById(R.id.actionIcon)
    private val reorderIcon: ImageView = view.findViewById(R.id.reorderImage)
    private var holdColor: Int? = null
    private var textColor: Int? = null
    private val defaultTextViewColor: Int = label.currentTextColor
    private val defaultBackgroundColor: Int = (view.background as ColorDrawable).color
    var underColor: Int? = null
        private set

    init {
        @Suppress("UNUSED_VARIABLE")
        @SuppressLint("ClickableViewAccessibility")
        val t = reorderIcon.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                touchHelper.startDrag(this)
                true
            } else {
                false
            }
        }
    }

    fun bind(action: HomeAction) {
        label.text = action.label(label.context)
        val drawableIcon = action.icon(icon.context)
        icon.setImageDrawable(drawableIcon)
        Palette.from(drawableIcon.toBitmap()).generate { palette ->
            var newColor = palette?.lightVibrantSwatch
            if (newColor == null || newColor.rgb == Color.WHITE) {
                newColor = palette?.dominantSwatch
            }
            holdColor = newColor?.rgb
            textColor = newColor?.bodyTextColor
            underColor = palette?.lightMutedSwatch?.rgb ?: holdColor?.let(::getUnderViewColor)
        }
        lastAction = action
    }

    private fun getUnderViewColor(viewColor: Int): Int {
        val a = Color.alpha(viewColor)
        val r = Color.red(viewColor)
        val g = Color.green(viewColor)
        val b = Color.blue(viewColor)
        return Color.argb(a / 8, r, g, b)
    }

    fun action(): HomeAction? = lastAction

    fun setOnHold(onHold: Boolean) {
        if (!onHold) {
            itemView.setBackgroundColor(defaultBackgroundColor)
            label.setTextColor(defaultTextViewColor)
            reorderIcon.setColorFilter(defaultTextViewColor)
        } else {
            holdColor?.also { holdColor ->
                textColor?.also { textColor ->
                    listOf(
                        ObjectAnimator.ofObject(
                            itemView,
                            "backgroundColor",
                            ArgbEvaluator(),
                            defaultBackgroundColor,
                            holdColor
                        ),
                        ObjectAnimator.ofObject(label, "textColor", ArgbEvaluator(), label.currentTextColor, textColor),
                        ObjectAnimator.ofObject(
                            reorderIcon,
                            "colorFilter",
                            ArgbEvaluator(),
                            defaultTextViewColor,
                            textColor
                        )
                    ).forEach {
                        it.apply {
                            duration = 250
                            start()
                        }
                    }
                }
            }
        }
    }
}
