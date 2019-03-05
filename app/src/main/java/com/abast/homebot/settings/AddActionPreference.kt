package com.abast.homebot.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.abast.homebot.R
import com.abast.homebot.actions.HomeAction
import kotlin.reflect.KClass

class AddActionPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.addActionPreferenceStyle,
    defStyleRes: Int = R.style.Preference_AddActionPreferenceStyle
) : DialogPreference(context, attrs, defStyleAttr, defStyleRes) {
    interface Listener {
        fun createAction(type: KClass<out HomeAction>)
    }
}
