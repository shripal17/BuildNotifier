package com.codertainment.buildnotifier.activity

import android.os.Bundle
import com.codertainment.buildnotifier.R
import com.codertainment.buildnotifier.fragment.SettingsFragment
import io.multimoon.colorful.CAppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : CAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_settings)

    setSupportActionBar(settings_toolbar)

    title = getString(R.string.title_settings)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    supportFragmentManager.beginTransaction().replace(R.id.settings_frame, SettingsFragment()).commitNow()
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }
}
