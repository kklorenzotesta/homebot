package com.abast.homebot.settings

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.abast.homebot.R
import com.google.android.material.snackbar.Snackbar
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class ActionListTouchHelper(
    context: Context,
    adapter: ActionListAdapter
) : ItemTouchHelper(
    object : ItemTouchHelper.Callback() {
        private val deleteIcon: Drawable =
            ContextCompat.getDrawable(context, R.drawable.ic_delete_sweep_black_24dp)!!.apply {
                setTint(context.getColor(android.R.color.holo_red_dark))
            }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int =
            makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START or ItemTouchHelper.END)

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            adapter.moveActions(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            (viewHolder as? ActionListViewHolder)?.action()?.let { action ->
                adapter.removeAction(action).also { index ->
                    Snackbar.make(viewHolder.itemView, R.string.deleted_quick_action_message, Snackbar.LENGTH_LONG)
                        .apply {
                            setAction(R.string.undo_delete_action_button_text) {
                                adapter.addAction(action, index)
                            }
                            show()
                        }
                }
            }
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                (viewHolder as? ActionListViewHolder)?.setOnHold(true)
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            (viewHolder as? ActionListViewHolder)?.setOnHold(false)
            super.clearView(recyclerView, viewHolder)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            val view = viewHolder.itemView
            val margin = view.height / 4
            val iconTop = view.top + margin
            val iconBottom = view.bottom - margin
            val iconSize = iconBottom - iconTop
            val extraSpace = ((dX.absoluteValue - iconSize) - (2 * margin)).roundToInt()
            val background = (viewHolder as ActionListViewHolder).underColor?.let { ColorDrawable(it) }
            when {
                dX > 0 -> {
                    val startingX = if (extraSpace < 0) view.left + margin + extraSpace else view.left + margin
                    deleteIcon.setBounds(
                        startingX,
                        iconTop,
                        startingX + iconSize,
                        iconBottom
                    )
                    background?.setBounds(0, view.top, view.left + dX.roundToInt(), view.bottom)
                }
                dX < 0 -> {
                    val endX = if (extraSpace < 0) view.right - margin - extraSpace else view.right - margin
                    deleteIcon.setBounds(endX - iconSize, iconTop, endX, iconBottom)
                    background?.setBounds(view.right + dX.roundToInt(), view.top, view.right, view.bottom)
                }
                else -> {
                    deleteIcon.setBounds(0, 0, 0, 0)
                    background?.setBounds(0, 0, 0, 0)
                }
            }
            background?.draw(c)
            deleteIcon.draw(c)
        }

        override fun isLongPressDragEnabled(): Boolean = true

        override fun isItemViewSwipeEnabled(): Boolean = true

    }
)
