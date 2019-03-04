package com.abast.homebot.actions

import androidx.annotation.StringRes
import com.abast.homebot.R
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.MINIMAL_CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(OpenWeb::class),
        JsonSubTypes.Type(LaunchApp::class),
        JsonSubTypes.Type(ToggleBrightness::class),
        JsonSubTypes.Type(OpenRecentApps::class),
        JsonSubTypes.Type(LaunchShortcut::class),
        JsonSubTypes.Type(ToggleFlashlight::class)
    ]
)
@JsonIgnoreProperties(ignoreUnknown = true)
sealed class HomeAction {
    @get:JsonIgnore
    @get:StringRes
    abstract val titleRes: Int
}

object ToggleFlashlight : HomeAction() {
    override val titleRes: Int
        get() = R.string.pref_title_flashlight
}

object ToggleBrightness : HomeAction() {
    override val titleRes: Int
        get() = R.string.pref_title_brightness
}

object OpenRecentApps : HomeAction() {
    override val titleRes: Int
        get() = R.string.pref_title_prev_app
}

data class LaunchApp(val uri: String) : HomeAction() {
    override val titleRes: Int
        get() = R.string.pref_title_app
}

data class LaunchShortcut(val uri: String) : HomeAction() {
    override val titleRes: Int
        get() = R.string.pref_title_shortcut
}

data class OpenWeb(val address: String) : HomeAction() {
    override val titleRes: Int
        get() = R.string.pref_title_web
}

/*enum class HomeAction(@StringRes val titleRes: Int, val repeatable: Boolean = false) {

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
}*/
