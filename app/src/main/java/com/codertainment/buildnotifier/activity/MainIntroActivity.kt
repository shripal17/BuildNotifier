package com.codertainment.buildnotifier.activity

import android.os.Bundle
import com.codertainment.buildnotifier.KEY_QUICK_START
import com.codertainment.buildnotifier.R
import com.codertainment.buildnotifier.fragment.SampleSlideFragment
import com.codertainment.buildnotifier.getPrefs
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPage
import com.mcxiaoke.koi.ext.find

class MainIntroActivity : AppIntro2() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    showSkipButton(false)

    val slide1 = SliderPage().apply {
      title = getString(R.string.intro_screen_1_title)
      description = getString(R.string.intro_screen_1_description)
      imageDrawable = R.drawable.ic_developer_activity
    }

    val slide3 = SliderPage().apply {
      title = getString(R.string.intro_screen_3_title)
      description = getString(R.string.intro_screen_3_description)
      imageDrawable = R.drawable.ic_relaxation
    }

    addSlide(AppIntroFragment.newInstance(slide1))
    addSlide(SampleSlideFragment.newInstance(R.layout.intro_screen_2))
    addSlide(AppIntroFragment.newInstance(slide3))
  }

  override fun onDonePressed(currentFragment: androidx.fragment.app.Fragment) {
    super.onDonePressed(currentFragment)
    getPrefs().saveBool(KEY_QUICK_START, true)
    startActivity(MainActivity.newIntent(this, find(R.id.done)))
    finish()
  }
}
