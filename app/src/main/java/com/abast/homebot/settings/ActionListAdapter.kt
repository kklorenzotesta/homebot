package com.abast.homebot.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.abast.homebot.R
import com.abast.homebot.actions.HomeAction
import java.util.*

class ActionListAdapter(context: Context, val onActionClick: (HomeAction) -> Unit) :
    RecyclerView.Adapter<ActionListViewHolder>() {
    val touchHelper: ItemTouchHelper by lazy { ActionListTouchHelper(context, this) }

    private val items: MutableList<HomeAction> = mutableListOf()

    override fun getItemCount(): Int =
        items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.action_list_item_layout, parent, false)
        return ActionListViewHolder(view, touchHelper)
    }

    override fun onBindViewHolder(holder: ActionListViewHolder, position: Int) {
        items[position].also { action ->
            holder.bind(action)
            holder.itemView.setOnClickListener {
                onActionClick(action)
            }
        }
    }

    fun addAction(action: HomeAction, index: Int? = null) {
        if (!items.contains(action)) {
            notifyItemInserted(
                if (index != null && index >= 0 && index <= items.size) {
                    items.add(index, action)
                    index
                } else {
                    items.add(action)
                    itemCount - 1
                }
            )
        }
    }

    fun setActions(actions: List<HomeAction>) {
        items.clear()
        items.addAll(actions)
        notifyDataSetChanged()
    }

    fun removeAction(action: HomeAction): Int {
        val index = items.indexOf(action)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
        return index
    }

    fun currentItems(): List<HomeAction> = items

    fun moveActions(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }
}
