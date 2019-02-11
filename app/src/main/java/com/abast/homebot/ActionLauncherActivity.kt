package com.abast.homebot

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.abast.homebot.actions.HomeAction
import kotlinx.android.synthetic.main.activity_launcher.*
import java.net.URISyntaxException

class ActionLauncherActivity : AppCompatActivity() {
    private val sharedPrefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }
    private val actions: List<Pair<HomeAction, String>> by lazy {
        HomeAction.values().filter { it.isSet(sharedPrefs) }
            .flatMap { ac -> ac.content(sharedPrefs).map { Pair(ac, it.first) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        launcher_background.setOnClickListener {
            finish()
        }
        if (actions.size == 1) {
            actions.first().apply {
                handleAction(first, second)
            }
        }
    }

    private fun handleAction(action: HomeAction?, value: String) {
        when (action) {
            HomeAction.TOGGLE_FLASHLIGHT -> toggleFlashlight()
            HomeAction.TOGGLE_BRIGHTNESS -> toggleBrightness()
            HomeAction.OPEN_RECENT_APPS -> openRecents()
            HomeAction.OPEN_WEB -> openWebAddress(value)
            HomeAction.LAUNCH_SHORTCUT, HomeAction.LAUNCH_APP -> launchUri(value)
            else -> launchMainActivity()
        }
    }

    /**
     * Launches MainActivity. Used as fallback for any errors that might occur.
     */
    private fun launchMainActivity() {
        val i = Intent(this, MainActivity::class.java)
        finish()
        startActivity(i)
    }

    /**
     * Launches a Uri. Used for launching shortcuts and activities.
     */
    private fun launchUri(uri: String?) {
        if (uri != null) {
            try {
                val shortcut = Intent.parseUri(uri, 0)
                startActivity(shortcut)
                finish()
            } catch (e: URISyntaxException) {
                e.printStackTrace()
                launchMainActivity()
            }
        }
    }

    /**
     * Toggles flashlight via a Service
     */
    private fun toggleFlashlight() {
        val flashlightIntent = Intent(this, FlashlightService::class.java)
        ContextCompat.startForegroundService(this, flashlightIntent)
        finish()
    }

    /**
     * Opens the recent apps screen
     * Source: http://stackoverflow.com/a/15964856/404784
     */
    private fun openRecents() {
        try {
            val serviceManagerClass = Class.forName("android.os.ServiceManager")
            val getService = serviceManagerClass.getMethod("getService", String::class.java)
            val retbinder = getService.invoke(serviceManagerClass, "statusbar") as IBinder
            val statusBarClass = Class.forName(retbinder.interfaceDescriptor!!)
            val statusBarObject = statusBarClass.classes[0].getMethod("asInterface", IBinder::class.java)
                .invoke(null, *arrayOf<Any>(retbinder))
            val clearAll = statusBarClass.getMethod("toggleRecentApps")
            clearAll.isAccessible = true
            clearAll.invoke(statusBarObject)
            finish()
        } catch (ex: Exception) {
            ex.printStackTrace()
            launchMainActivity()
        }
    }

    /**
     * Toggles brightness between maximum and minimum.
     */
    private fun toggleBrightness() {
        if (Settings.System.canWrite(this)) {
            val cResolver = contentResolver
            try {
                // To handle the auto
                Settings.System.putInt(
                    cResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                //Get the current system brightness
                val brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS)
                if (brightness > 0) {
                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 0)
                } else {
                    Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 255)
                }
                finish()
            } catch (e: Settings.SettingNotFoundException) {
                Log.e("Error", getString(R.string.error_brightness))
                Toast.makeText(this, R.string.error_brightness, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                launchMainActivity()
            }
        } else {
            launchMainActivity()
        }
    }

    /**
     * Opens web browser pointing to given url
     */
    private fun openWebAddress(url: String?) {
        if (url != null) {
            var finalUrl = url
            if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://"))
                finalUrl = "http://$url"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
            startActivity(browserIntent)
            finish()
        } else {
            launchMainActivity()
        }
    }

}
