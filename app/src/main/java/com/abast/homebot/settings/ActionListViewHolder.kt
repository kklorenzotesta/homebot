package com.abast.homebot.settings

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.abast.homebot.R
import com.abast.homebot.actions.HomeAction

class ActionListViewHolder(view: View, private val touchHelper: ItemTouchHelper) : RecyclerView.ViewHolder(view) {
    private var lastAction: HomeAction? = null

    private val label: TextView = view.findViewById(R.id.action_label)
    private val icon: ImageView = view.findViewById(R.id.actionIcon)
    private val reorderIcon: ImageView = view.findViewById(R.id.reorderImage)

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
        icon.setImageDrawable(action.icon(icon.context))
        lastAction = action
    }

    fun action(): HomeAction? = lastAction
}
