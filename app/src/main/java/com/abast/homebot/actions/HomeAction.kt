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

    private fun summaryKey(): String = key() + "_summary"

    fun values(sharedPreferences: SharedPreferences): Set<String> =
        if (repeatable) {
            sharedPreferences.getStringSet(key(), null) ?: emptySet()
        } else {
            sharedPreferences.getString(key(), null)?.let { setOf(it) } ?: emptySet()
        }

    fun summaries(sharedPreferences: SharedPreferences): Set<String> =
        if (repeatable) {
            (sharedPreferences.getStringSet(summaryKey(), null) ?: emptySet())
        } else {
            sharedPreferences.getString(summaryKey(), null)?.let { setOf(it) } ?: emptySet()
        }

    fun addValue(value: String, summary: String, sharedPreferences: SharedPreferences) {
        if (repeatable) {
            sharedPreferences.edit()
                .putStringSet(
                    key(),
                    (sharedPreferences.getStringSet(key(), null) ?: emptySet()) + value
                )
                .putStringSet(
                    summaryKey(),
                    (sharedPreferences.getStringSet(summaryKey(), null) ?: emptySet()) + summary
                )
                .apply()
        } else {
            sharedPreferences.edit().putString(key(), value)
                .putString(summaryKey(), summary).apply()
        }
    }

    fun removeValue(value: String, summary: String, sharedPreferences: SharedPreferences) {
        if (repeatable) {
            sharedPreferences.edit()
                .putStringSet(
                    key(),
                    (sharedPreferences.getStringSet(key(), null) ?: emptySet()) - value
                )
                .putStringSet(
                    summaryKey(),
                    (sharedPreferences.getStringSet(summaryKey(), null) ?: emptySet()) - summary
                )
                .apply()
        } else {
            sharedPreferences.edit().remove(key()).remove(summaryKey()).apply()
        }
    }

    fun isSet(sharedPreferences: SharedPreferences): Boolean = if (this.repeatable) {
        (sharedPreferences.getStringSet(key(), null) ?: emptySet()).isNotEmpty()
    } else {
        (sharedPreferences.getString(key(), null) ?: "").isNotEmpty()
    }
}