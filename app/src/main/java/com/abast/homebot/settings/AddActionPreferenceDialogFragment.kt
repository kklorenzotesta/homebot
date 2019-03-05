package com.abast.homebot.settings

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import com.abast.homebot.actions.HomeAction
import com.abast.homebot.actions.dumbInstance
import kotlin.reflect.KClass

class AddActionPreferenceDialogFragment : PreferenceDialogFragmentCompat() {
    companion object {
        fun newInstance(key: String): AddActionPreferenceDialogFragment {
            val fragment = AddActionPreferenceDialogFragment()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }

    private val entries: List<KClass<out HomeAction>> by lazy {
        HomeAction::class.sealedSubclasses
    }

    private val entriesText by lazy {
        entries.map {
            getString(it.dumbInstance().titleRes)
        }
    }

    private var clickedItemIndex: Int? = null
    var listener: AddActionPreference.Listener? = null

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)
        builder.setItems(entriesText.toTypedArray()) { dialog, index ->
            clickedItemIndex = index
            this.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dialog.dismiss()
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            clickedItemIndex?.let { clickedItemIndex ->
                listener?.createAction(entries[clickedItemIndex])
                listener = null
            }
        }
    }
}