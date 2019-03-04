package com.abast.homebot.settings

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abast.homebot.R
import com.abast.homebot.actions.HomeAction

class ActionListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private var lastAction: HomeAction? = null

    private val label: TextView = view.findViewById(R.id.action_label)
    private val icon: ImageView = view.findViewById(R.id.actionIcon)

    fun bind(action: HomeAction) {
        label.text = action.label(label.context)
        icon.setImageDrawable(action.icon(icon.context))
        lastAction = action
    }

    fun action(): HomeAction? = lastAction
}
