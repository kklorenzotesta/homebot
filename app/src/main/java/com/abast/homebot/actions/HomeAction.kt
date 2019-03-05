package com.abast.homebot.actions

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import com.abast.homebot.R
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.lang.ref.SoftReference
import kotlin.reflect.KClass

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

    fun title(context: Context): String = context.getString(titleRes)

    abstract fun label(context: Context): String

    abstract fun icon(context: Context): Drawable
}

fun KClass<out HomeAction>.dumbInstance(): HomeAction = when (this) {
    ToggleFlashlight::class -> ToggleFlashlight
    ToggleBrightness::class -> ToggleBrightness
    OpenRecentApps::class -> OpenRecentApps
    LaunchApp::class -> LaunchApp("")
    LaunchShortcut::class -> LaunchShortcut("", "")
    OpenWeb::class -> OpenWeb("")
    else -> throw IllegalStateException()
}

object ToggleFlashlight : HomeAction() {
    override fun icon(context: Context): Drawable = context.getDrawable(R.drawable.ic_launcher_foreground_green)!!
    override fun label(context: Context): String = title(context)
    override val titleRes: Int
        get() = R.string.pref_title_flashlight
}

object ToggleBrightness : HomeAction() {
    override fun icon(context: Context): Drawable = context.getDrawable(R.drawable.ic_launcher_foreground_green)!!
    override fun label(context: Context): String = title(context)
    override val titleRes: Int
        get() = R.string.pref_title_brightness
}

object OpenRecentApps : HomeAction() {
    override fun icon(context: Context): Drawable = context.getDrawable(R.drawable.ic_launcher_foreground_green)!!
    override fun label(context: Context): String = title(context)
    override val titleRes: Int
        get() = R.string.pref_title_prev_app
}

data class LaunchApp(val uri: String) : HomeAction() {
    private var iconCache: SoftReference<Drawable>? = null

    override fun icon(context: Context): Drawable {
        val cache = iconCache?.get()
        return if (cache == null) {
            val newIcon = context.packageManager.getActivityIcon(Intent.parseUri(uri, 0))
            iconCache = SoftReference(newIcon)
            newIcon
        } else {
            cache
        }
    }

    override fun label(context: Context): String =
        context.packageManager.resolveActivity(
            Intent.parseUri(uri, 0),
            0
        ).activityInfo.loadLabel(context.packageManager).toString()

    override val titleRes: Int
        get() = R.string.pref_title_app
}

data class LaunchShortcut(val uri: String, val name: String) : HomeAction() {
    override fun icon(context: Context): Drawable = context.packageManager.getActivityIcon(Intent.parseUri(uri, 0))
    override fun label(context: Context): String = name
    override val titleRes: Int
        get() = R.string.pref_title_shortcut
}

data class OpenWeb(val address: String) : HomeAction() {
    override fun icon(context: Context): Drawable = context.getDrawable(R.drawable.ic_launcher_foreground_green)!!
    override fun label(context: Context): String = address
    override val titleRes: Int
        get() = R.string.pref_title_web
}
