package com.abast.homebot.settings

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ActionListTouchHelper(
    preference: ActionsPreference
) : ItemTouchHelper(
    object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int =
            makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START or ItemTouchHelper.END)

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean =
            (viewHolder as? ActionListViewHolder)?.action()?.let { a1 ->
                (target as? ActionListViewHolder)?.action()?.let { a2 ->
                    preference.swapActions(a1, a2)
                    true
                }
            } ?: false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            (viewHolder as? ActionListViewHolder)?.action()?.let { action -> preference.removeAction(action) }
        }

    }
)
