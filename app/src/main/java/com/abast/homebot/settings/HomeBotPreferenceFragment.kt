package com.abast.homebot.settings

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.abast.homebot.R
import com.abast.homebot.actions.*
import com.abast.homebot.pickers.AppPickerActivity
import kotlin.reflect.KClass

class HomeBotPreferenceFragment : PreferenceFragmentCompat(), AddActionPreference.Listener {
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

    private val actionsPreference: ActionsPreference by lazy {
        findPreference<ActionsPreference>(getString(R.string.actions_setting_key))
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is AddActionPreference) {
            AddActionPreferenceDialogFragment.newInstance(preference.key).let { dialog ->
                dialog.setTargetFragment(this, 0)
                dialog.listener = this
                dialog.show(fragmentManager!!, "androidx.preference.PreferenceFragment.DIALOG")
            }
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun createAction(type: KClass<out HomeAction>) {
        when (type) {
            ToggleBrightness::class, ToggleFlashlight::class, OpenRecentApps::class -> actionsPreference.addAction(
                type.dumbInstance()
            )
            LaunchApp::class -> startActivityForResult(
                appPickerIntent,
                AppPickerActivity.REQUEST_CODE_APP
            )
            LaunchShortcut::class -> startActivityForResult(
                shortcutPickerIntent,
                AppPickerActivity.REQUEST_CODE_SHORTCUT
            )
            OpenWeb::class -> showWebDialog()
        }
    }

    /**
     * Shows dialog with EditText to enter a url
     */
    private fun showWebDialog() {
        context?.let {
            val et = EditText(it)
            et.hint = getString(R.string.web_hint)
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.enter_url)
            builder.setView(et)
            builder.setPositiveButton(R.string.ok) { dialog, _ ->
                actionsPreference.addAction(OpenWeb(et.text.toString()))
                dialog.dismiss()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("KHB", "outer result")
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
                AppPickerActivity.REQUEST_CODE_SHORTCUT -> {
                    val shortcutIntent = data?.extras?.getParcelable<Intent>(Intent.EXTRA_SHORTCUT_INTENT)!!
                    val shortcutName = data.extras?.getString(Intent.EXTRA_SHORTCUT_NAME)!!
                    val value = shortcutIntent.toUri(Intent.URI_INTENT_SCHEME)
                    actionsPreference.addAction(LaunchShortcut(value, shortcutName))
                }
            }
        }
    }

    /*override fun onResume() {
        super.onResume()

        // Check if we have brightness permission
        val brightnessSwitch = findSwitch(HomeAction.TOGGLE_BRIGHTNESS)
        if (!Settings.System.canWrite(activity) && brightnessSwitch.isChecked) {
            brightnessSwitch.isChecked = false
        }
    }

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
    }*/

}
