package com.abast.homebot.actions

import android.content.SharedPreferences
import androidx.annotation.StringRes
import com.abast.homebot.R

enum class HomeAction(@StringRes val titleRes: Int, val repeatable: Boolean = false) {
    LAUNCH_APP(R.string.pref_title_app, true),
    LAUNCH_SHORTCUT(R.string.pref_title_shortcut, true),
    TOGGLE_FLASHLIGHT(R.string.pref_title_flashlight),
    TOGGLE_BRIGHTNESS(R.string.pref_title_brightness),
    OPEN_WEB(R.string.pref_title_web, true),
    OPEN_RECENT_APPS(R.string.pref_title_prev_app);

    fun switchKey(): String = this.name.toLowerCase()

    private fun key(): String = switchKey() + "_key"

    private fun summaryKey(value: String): String = key() + "_" + value + "_summary"

    fun content(sharedPreferences: SharedPreferences): List<Pair<String, String>> =
        values(sharedPreferences).map {
            Pair(
                it,
                sharedPreferences.getString(summaryKey(it), null) ?: ""
            )
        }

    private fun values(sharedPreferences: SharedPreferences): Set<String> =
        if (repeatable) {
            sharedPreferences.getStringSet(key(), null) ?: emptySet()
        } else {
            sharedPreferences.getString(key(), null)?.let { setOf(it) } ?: emptySet()
        }

    fun addValue(value: String, summary: String, sharedPreferences: SharedPreferences) {
        if (repeatable) {
            sharedPreferences.edit()
                .putStringSet(
                    key(),
                    (sharedPreferences.getStringSet(key(), null) ?: emptySet()) + value
                )
                .putString(summaryKey(value), summary)
                .apply()
        } else {
            sharedPreferences.edit().putString(key(), value)
                .putString(summaryKey(value), summary).apply()
        }
    }

    fun removeValue(value: String, sharedPreferences: SharedPreferences) {
        if (repeatable) {
            sharedPreferences.edit()
                .putStringSet(
                    key(),
                    (sharedPreferences.getStringSet(key(), null) ?: emptySet()) - value
                )
                .remove(summaryKey(value))
                .apply()
        } else {
            sharedPreferences.edit().remove(key()).remove(summaryKey(value)).apply()
        }
    }

    fun isSet(sharedPreferences: SharedPreferences): Boolean = if (this.repeatable) {
        (sharedPreferences.getStringSet(key(), null) ?: emptySet()).isNotEmpty()
    } else {
        (sharedPreferences.getString(key(), null) ?: "").isNotEmpty()
    }
}