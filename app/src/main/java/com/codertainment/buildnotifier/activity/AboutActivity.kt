package com.codertainment.buildnotifier.activity

import android.os.Bundle
import com.codertainment.buildnotifier.R
import com.codertainment.buildnotifier.fragment.AboutFragment
import io.multimoon.colorful.CAppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : CAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_about)

    setSupportActionBar(about_toolbar)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    title = String.format(getString(R.string.about_app), getString(R.string.app_name))

    supportFragmentManager.beginTransaction().replace(R.id.about_frame, AboutFragment()).commit()
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }
}
