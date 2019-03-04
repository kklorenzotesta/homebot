package com.abast.homebot.settings

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abast.homebot.R
import com.abast.homebot.actions.HomeAction

class ActionListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val label: TextView = view.findViewById(R.id.action_label)

    init {
        Log.d("HB", "created view holder weeeee!")
    }

    fun bind(action: HomeAction) {
        label.text = action.toString()
    }
}
