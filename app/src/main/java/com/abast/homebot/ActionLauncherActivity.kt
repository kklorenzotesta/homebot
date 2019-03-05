package com.abast.homebot

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.abast.homebot.actions.HomeAction
import com.abast.homebot.views.QuickActionButton
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.android.synthetic.main.activity_launcher.*

class ActionLauncherActivity : AppCompatActivity() {
    private val sharedPrefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }
    private val mapper: ObjectMapper by lazy { jacksonObjectMapper() }
    private val actionsReader: ObjectReader by lazy { mapper.readerFor(object : TypeReference<List<HomeAction>>() {}) }

    private val actions: List<HomeAction> by lazy {
        val string = sharedPrefs.getString(getString(R.string.actions_setting_key), "[]")
        actionsReader.readValue<List<HomeAction>>(string)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        launcher_background.setOnClickListener {
            finish()
        }
        if (actions.size == 1) {
            actions.first().run(this)
        } else if (actions.isEmpty()) {
            launchMainActivity()
        } else {
            actions.map {
                QuickActionButton(this).apply {
                    setAction(it)
                }
            }.also {
                launcher_background.setButtons(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        finish()
    }

    /**
     * Launches MainActivity. Used as fallback for any errors that might occur.
     */
    private fun launchMainActivity() {
        val i = Intent(this, MainActivity::class.java)
        finish()
        startActivity(i)
    }

}
