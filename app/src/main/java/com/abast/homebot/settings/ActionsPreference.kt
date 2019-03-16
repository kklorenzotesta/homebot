package com.abast.homebot.settings

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abast.homebot.R
import com.abast.homebot.actions.HomeAction
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.lang.Exception

class ActionsPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.actionsPreferenceStyle,
    defStyleRes: Int = R.style.Preference_ActionsPreferenceStyle
) : Preference(context, attrs, defStyleAttr, defStyleRes) {
    inner class AdapterDataObserver : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            setValue(adapter.currentItems())
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            setValue(adapter.currentItems())
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            setValue(adapter.currentItems())
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            setValue(adapter.currentItems())
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            setValue(adapter.currentItems())
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            setValue(adapter.currentItems())
        }
    }

    private val mapper: ObjectMapper by lazy { jacksonObjectMapper() }
    private val typeRef: TypeReference<List<HomeAction>> by lazy {
        object : TypeReference<List<HomeAction>>() {}
    }
    private val writer: ObjectWriter by lazy { mapper.writerFor(typeRef) }
    private val reader: ObjectReader by lazy { mapper.readerFor(typeRef) }
    private val adapter: ActionListAdapter by lazy {
        ActionListAdapter(context, ::onActionClicked).apply {
            registerAdapterDataObserver(AdapterDataObserver())
        }
    }
    private var clickObserver: ((HomeAction) -> Unit)? = null
    private var list: RecyclerView? = null
    private var emptyMessage: TextView? = null

    override fun onSetInitialValue(defaultValue: Any?) {
        getValue().also { actions ->
            adapter.setActions(actions)
            updateVisibilities(actions.isEmpty())
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        list = (holder.findViewById(R.id.actions_preference_list) as? RecyclerView)
        emptyMessage = (holder.findViewById(R.id.actions_preference_empty_message) as? TextView)
        list?.let { list ->
            list.layoutManager = LinearLayoutManager(context)
            list.adapter = adapter
            adapter.touchHelper.attachToRecyclerView(list)
        }
        updateVisibilities(getValue().isEmpty())
    }

    private fun setValue(value: List<HomeAction>) {
        persistString(writer.writeValueAsString(value))
        updateVisibilities(value.isEmpty())
    }

    private fun updateVisibilities(empty: Boolean) {
        if (empty) {
            list?.visibility = View.GONE
            emptyMessage?.visibility = View.VISIBLE
        } else {
            list?.visibility = View.VISIBLE
            emptyMessage?.visibility = View.GONE
        }
    }

    private fun getValue(): List<HomeAction> =
        try {
            reader.readValue(getPersistedString(""))
        } catch (_: Exception) {
            setValue(emptyList())
            adapter.setActions(emptyList())
            emptyList()
        }

    fun refresh() {
        onSetInitialValue(emptyList<HomeAction>())
    }

    fun addAction(action: HomeAction) {
        adapter.addAction(action)
    }

    fun setOnActionClickListener(listener: ((HomeAction) -> Unit)?) {
        clickObserver = listener
    }

    private fun onActionClicked(action: HomeAction) {
        clickObserver?.invoke(action)
    }
}
