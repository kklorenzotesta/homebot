package com.abast.homebot.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.preference.*
import com.abast.homebot.R
import com.abast.homebot.actions.HomeAction
import com.abast.homebot.actions.LaunchApp
import com.abast.homebot.pickers.AppPickerActivity
import kotlin.reflect.KClass

class HomeBotPreferenceFragment : PreferenceFragmentCompat() {
    companion object {
        const val VALUE_EXTRA_KEY = "pref_value"
    }

    private val appPickerIntent: Intent by lazy {
        Intent(context, AppPickerActivity::class.java).apply {
            putExtra(AppPickerActivity.EXTRA_LABEL, getString(R.string.choose_app))
            putExtra(AppPickerActivity.EXTRA_PICK_TYPE, AppPickerActivity.PICK_TYPE_APP)
        }
    }

    private val shortcutPickerIntent: Intent by lazy {
        Intent(context, AppPickerActivity::class.java).apply {
            putExtra(AppPickerActivity.EXTRA_LABEL, getString(R.string.choose_shortcut))
            putExtra(AppPickerActivity.EXTRA_PICK_TYPE, AppPickerActivity.PICK_TYPE_SHORTCUT)
        }
    }

    private val addActionPreference: Preference by lazy {
        findPreference<Preference>(getString(R.string.add_action_preference_key))
    }

    private val actionsPreference: ActionsPreference by lazy {
        findPreference<ActionsPreference>(getString(R.string.actions_setting_key))
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
        addActionPreference.setOnPreferenceClickListener {
            startActivityForResult(appPickerIntent, AppPickerActivity.REQUEST_CODE_APP)
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppPickerActivity.REQUEST_CODE_APP -> {
                    data?.extras?.getParcelable<ActivityInfo>(AppPickerActivity.EXTRA_PICKED_CONTENT)?.let { appData ->
                        val activityIntent = Intent()
                        activityIntent.setClassName(appData.packageName, appData.name)
                        val value = activityIntent.toUri(Intent.URI_INTENT_SCHEME)
                        actionsPreference.addAction(LaunchApp(value))
                    }
                }
                /*AppPickerActivity.REQUEST_CODE_SHORTCUT -> {
                    val shortcutIntent = data?.extras?.getParcelable<Intent>(Intent.EXTRA_SHORTCUT_INTENT)!!
                    val shortcutName = data.extras?.getString(Intent.EXTRA_SHORTCUT_NAME)!!
                    val value = shortcutIntent.toUri(Intent.URI_INTENT_SCHEME)
                    categories[HomeAction.LAUNCH_SHORTCUT]!!.addPreference(SwitchPreference(context).apply {
                        title = shortcutName
                        key = HomeAction.LAUNCH_SHORTCUT.switchKey() + value
                        isChecked = true
                        extras.putString(VALUE_EXTRA_KEY, value)
                        switches[this] = HomeAction.LAUNCH_SHORTCUT
                    })
                    HomeAction.LAUNCH_SHORTCUT.addValue(value, shortcutName, sharedPreferences)
                }*/
            }
        }
    }

    /*override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        val switch = preference as SwitchPreference
        if (switch.isChecked) {
            val action: HomeAction? = switches[switch]
            when (action) {
                HomeAction.LAUNCH_APP -> {
                    switch.isChecked = false
                    startActivityForResult(appPickerIntent, AppPickerActivity.REQUEST_CODE_APP)
                }
                HomeAction.LAUNCH_SHORTCUT -> {
                    switch.isChecked = false
                    startActivityForResult(
                        shortcutPickerIntent,
                        AppPickerActivity.REQUEST_CODE_SHORTCUT
                    )
                }
                HomeAction.TOGGLE_BRIGHTNESS -> {
                    askForBrightnessPermission()
                }
                HomeAction.OPEN_WEB -> {
                    switch.isChecked = false
                    showWebDialog { setLaunchUrl(it) }
                }
                null -> Unit
                else -> action.addValue("true", "", sharedPreferences)
            }
        } else {
            switches[switch]!!.removeValue(
                switch.extras.getString(VALUE_EXTRA_KEY) ?: "",
                sharedPreferences
            )
            if (switches[switch]!!.repeatable) {
                categories[switches[switch]!!]!!.removePreference(switch)
            } else {
                switch.summary = null
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onResume() {
        super.onResume()

        // Check if we have brightness permission
        val brightnessSwitch = findSwitch(HomeAction.TOGGLE_BRIGHTNESS)
        if (!Settings.System.canWrite(activity) && brightnessSwitch.isChecked) {
            brightnessSwitch.isChecked = false
        }
    }

    private fun findSwitch(action: HomeAction): SwitchPreference =
        switches.entries.find { it.value == action }!!.key

    // ============== Utils ============== //

    /**
     * Opens settings app to enable settings write
     */
    private fun askForBrightnessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(activity)) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + activity!!.packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    /**
     * Shows dialog with EditText to enter a url
     */
    private fun showWebDialog(onInput: (String) -> Unit) {
        context?.let {
            val et = EditText(it)
            et.hint = getString(R.string.web_hint)
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.enter_url)
            builder.setView(et)
            builder.setPositiveButton(R.string.ok) { dialog, _ ->
                onInput.invoke(et.text.toString())
                dialog.dismiss()
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ ->
                findSwitch(HomeAction.OPEN_WEB).isChecked = false
                dialog.cancel()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    /**
     * Sets the web url to launch
     */
    private fun setLaunchUrl(url: String) {
        categories[OpenWeb::class]!!.addPreference(SwitchPreference(context).apply {
            title = url
            key = HomeAction.OPEN_WEB.switchKey() + url
            isChecked = true
            extras.putString(VALUE_EXTRA_KEY, url)
            switches[this] = HomeAction.OPEN_WEB
        })
        HomeAction.OPEN_WEB.addValue(url, url, sharedPreferences)
    }*/

}
