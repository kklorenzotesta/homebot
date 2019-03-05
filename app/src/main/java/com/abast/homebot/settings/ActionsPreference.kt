package com.abast.homebot.settings

import android.content.Context
import android.util.AttributeSet
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

class ActionsPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.actionsPreferenceStyle,
    defStyleRes: Int = R.style.Preference_ActionsPreferenceStyle
) : Preference(context, attrs, defStyleAttr, defStyleRes) {
    private val mapper: ObjectMapper by lazy { jacksonObjectMapper() }
    private val typeRef: TypeReference<List<HomeAction>> by lazy {
        object : TypeReference<List<HomeAction>>() {}
    }
    private val writer: ObjectWriter by lazy { mapper.writerFor(typeRef) }
    private val reader: ObjectReader by lazy { mapper.readerFor(typeRef) }
    private val adapter: ActionListAdapter by lazy {
        ActionListAdapter(context).also {
            it.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
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
            })
        }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        adapter.setActions(getValue())
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        (holder.findViewById(R.id.actions_preference_list) as? RecyclerView)?.let { list ->
            list.layoutManager = LinearLayoutManager(context)
            list.adapter = adapter
            adapter.touchHelper.attachToRecyclerView(list)
        }
    }

    private fun setValue(value: List<HomeAction>) {
        persistString(writer.writeValueAsString(value))
    }

    private fun getValue(): List<HomeAction> =
        reader.readValue(getPersistedString(writer.writeValueAsString(emptyList<HomeAction>())))

    fun addAction(action: HomeAction) {
        adapter.addAction(action)
    }
}
