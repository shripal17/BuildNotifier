package com.codertainment.buildnotifier.activity

import android.os.Bundle
import android.os.Handler
import com.codertainment.buildnotifier.KEY_QUICK_START
import com.codertainment.buildnotifier.R
import com.codertainment.buildnotifier.getPrefs
import com.mcxiaoke.koi.ext.delayed
import com.mcxiaoke.koi.ext.startActivity
import io.multimoon.colorful.CAppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : CAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_splash)

    Handler().delayed(1500) {
      try {
        if (getPrefs().getBool(KEY_QUICK_START)) {
          startActivity(MainActivity.newIntent(this, splash_bg))
        } else {
          startActivity<MainIntroActivity>()
        }
        finish()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}
