package com.abast.homebot

import android.content.Context
import android.util.AttributeSet
import androidx.preference.*

class ActionsPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.actionsPreferenceStyle,
    defStyleRes: Int = R.style.Preference_ActionsPreferenceStyle
) : Preference(context, attrs, defStyleAttr, defStyleRes) {

}
