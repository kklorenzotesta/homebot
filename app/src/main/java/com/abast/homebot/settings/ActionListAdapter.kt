package com.abast.homebot.settings

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.abast.homebot.R
import com.abast.homebot.actions.HomeAction

class ActionListAdapter : ListAdapter<HomeAction, ActionListViewHolder>(
    object : DiffUtil.ItemCallback<HomeAction>() {
        override fun areItemsTheSame(oldItem: HomeAction, newItem: HomeAction): Boolean = (oldItem === newItem).also {
            Log.d("HB", "called areItemsTheSame $oldItem to $newItem")
        }

        override fun areContentsTheSame(oldItem: HomeAction, newItem: HomeAction): Boolean = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.action_list_item_layout, parent, false)
        return ActionListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActionListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
