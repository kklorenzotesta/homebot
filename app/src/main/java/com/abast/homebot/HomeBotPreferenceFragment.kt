package com.abast.homebot

import android.app.Activity.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.preference.*
import com.abast.homebot.actions.HomeAction
import com.abast.homebot.actions.OpenWeb
import com.abast.homebot.pickers.AppPickerActivity
import kotlin.reflect.KClass

class HomeBotPreferenceFragment : PreferenceFragmentCompat() {
    companion object {
        const val VALUE_EXTRA_KEY = "pref_value"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }
    private val switches: HashMap<SwitchPreference, HomeAction> = HashMap()
    private val categories: HashMap<KClass<out HomeAction>, PreferenceCategory> = HashMap()

    private lateinit var appPickerIntent: Intent
    private lateinit var shortcutPickerIntent: Intent

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Make intents for App and shortcut picker activities
        appPickerIntent = Intent(context, AppPickerActivity::class.java)
        appPickerIntent.putExtra(AppPickerActivity.EXTRA_LABEL, getString(R.string.choose_app))
        appPickerIntent.putExtra(AppPickerActivity.EXTRA_PICK_TYPE, AppPickerActivity.PICK_TYPE_APP)

        shortcutPickerIntent = Intent(context, AppPickerActivity::class.java)
        shortcutPickerIntent.putExtra(AppPickerActivity.EXTRA_LABEL, getString(R.string.choose_shortcut))
        shortcutPickerIntent.putExtra(AppPickerActivity.EXTRA_PICK_TYPE, AppPickerActivity.PICK_TYPE_SHORTCUT)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
        /*val shared = sharedPreferences
         Log.d("HomeBot", "shared is $shared")
         val (repeatables, nonRepeatables) = HomeAction.values().partition { it.repeatable }
         repeatables.forEach {
             val category = PreferenceCategory(context)
             preferenceScreen.addPreference(category)
             categories[it] = category
             category.addPreference(SwitchPreference(context).apply {
                 setTitle(it.titleRes)
                 key = it.switchKey()
                 isChecked = false
                 summary = ""
                 switches[this] = it
             })
             it.content(shared).forEach { (value, summary) ->
                 category.addPreference(SwitchPreference(context).apply {
                     title = summary
                     key = it.switchKey() + value
                     isChecked = true
                     extras.putString(VALUE_EXTRA_KEY, value)
                     switches[this] = it
                 })
             }
         }
         val nonRepeatableCategory = PreferenceCategory(context)
         preferenceScreen.addPreference(nonRepeatableCategory)
         nonRepeatables.forEach {
             nonRepeatableCategory.addPreference(SwitchPreference(context).apply {
                 setTitle(it.titleRes)
                 key = it.switchKey()
                 isChecked = it.isSet(shared)
                 summary = it.content(shared).firstOrNull()?.second ?: ""
                 switches[this] = it
             })
         }*/
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                AppPickerActivity.REQUEST_CODE_APP -> {
                    data?.extras?.getParcelable<ActivityInfo>(AppPickerActivity.EXTRA_PICKED_CONTENT)?.let { appData ->
                        val label = appData.loadLabel(activity?.packageManager!!).toString()
                        val activityIntent = Intent()
                        activityIntent.setClassName(appData.packageName, appData.name)
                        val value = activityIntent.toUri(Intent.URI_INTENT_SCHEME)
                        categories[HomeAction.LAUNCH_APP]!!.addPreference(SwitchPreference(context).apply {
                            title = label
                            key = HomeAction.LAUNCH_APP.switchKey() + value
                            isChecked = true
                            extras.putString(VALUE_EXTRA_KEY, value)
                            switches[this] = HomeAction.LAUNCH_APP
                        })
                        HomeAction.LAUNCH_APP.addValue(value, label, sharedPreferences)
                    }
                }
                AppPickerActivity.REQUEST_CODE_SHORTCUT -> {
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
                }
            }
        }
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
