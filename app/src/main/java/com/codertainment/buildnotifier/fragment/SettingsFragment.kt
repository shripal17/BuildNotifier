package com.codertainment.buildnotifier.fragment

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.codertainment.buildnotifier.*
import com.codertainment.buildnotifier.activity.MainIntroActivity
import com.droidman.ktoasty.showErrorToast
import com.droidman.ktoasty.showInfoToast
import com.droidman.ktoasty.showSuccessToast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.mcxiaoke.koi.ext.finish
import com.takisoft.preferencex.ColorPickerPreference
import com.takisoft.preferencex.PreferenceFragmentCompat
import com.takisoft.preferencex.RingtonePreference
import io.multimoon.colorful.Colorful
import io.multimoon.colorful.ThemeColor
import org.jetbrains.anko.support.v4.startActivity

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

  private lateinit var primaryColor: ColorPickerPreference
  private lateinit var accentColor: ColorPickerPreference
  private lateinit var darkTheme: SwitchPreference
  private lateinit var successTone: RingtonePreference
  private lateinit var failureTone: RingtonePreference
  private lateinit var fcmToken: Preference
  private lateinit var quickStart: Preference
  private lateinit var supportUs: Preference

  override fun onPreferenceChange(p0: Preference?, p1: Any?): Boolean {
    if (p0 != null && p1 != null) {
      if (p0.key == getString(R.string.key_primary_color) || p0.key == getString(R.string.key_accent_color) || p0.key == getString(R.string.key_dark_theme)) {
        if (p0 is ColorPickerPreference) {
          if (p0.key == getString(R.string.key_primary_color)) {
            primaryColorValue = p1 as Int
          } else if (p0.key == getString(R.string.key_accent_color)) {
            accentColorValue = p1 as Int
          }
        } else if (p0 is SwitchPreference) {
          if (p0.key == getString(R.string.key_dark_theme)) {
            isDarkTheme = p1 as Boolean
          }
        }
        Handler().postDelayed({ updateTheme() }, 200)
      }
    }
    return true
  }

  private var accentColorValue: Int = ThemeColor.DEEP_PURPLE.primaryStyle()
  private var primaryColorValue: Int = ThemeColor.DEEP_ORANGE.primaryStyle()
  private var isDarkTheme: Boolean = true
  private lateinit var colors: IntArray
  private val themes = arrayOf(
    ThemeColor.RED,
    ThemeColor.PINK,
    ThemeColor.PURPLE,
    ThemeColor.DEEP_PURPLE,
    ThemeColor.INDIGO,
    ThemeColor.BLUE,
    ThemeColor.LIGHT_BLUE,
    ThemeColor.CYAN,
    ThemeColor.TEAL,
    ThemeColor.GREEN,
    ThemeColor.LIGHT_GREEN,
    ThemeColor.LIME,
    ThemeColor.YELLOW,
    ThemeColor.AMBER,
    ThemeColor.ORANGE,
    ThemeColor.DEEP_ORANGE,
    ThemeColor.BROWN,
    ThemeColor.GREY,
    ThemeColor.BLUE_GREY,
    ThemeColor.WHITE,
    ThemeColor.BLACK
  )

  override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
    preferenceManager.sharedPreferencesName = "prefs"
    setPreferencesFromResource(R.xml.settings, rootKey)
    colors = resources.getIntArray(R.array.default_colors)
    bind()
    accentColorValue = Colorful().getAccentColor().getColorPack().normal().asInt()
    primaryColorValue = Colorful().getPrimaryColor().getColorPack().normal().asInt()
    isDarkTheme = Colorful().getDarkTheme()
  }

  private fun bind() {
    primaryColor = findPreference(getString(R.string.key_primary_color)) as ColorPickerPreference
    primaryColor.onPreferenceChangeListener = this

    accentColor = findPreference(getString(R.string.key_accent_color)) as ColorPickerPreference
    accentColor.onPreferenceChangeListener = this

    darkTheme = findPreference(getString(R.string.key_dark_theme)) as SwitchPreference
    darkTheme.onPreferenceChangeListener = this

    fcmToken = findPreference(getString(R.string.key_fcm_token)).apply {
      setOnPreferenceClickListener {
        this@SettingsFragment.requireContext().showDeviceTokenDialog()
        true
      }
    }

    quickStart = findPreference(getString(R.string.key_quick_start))
    quickStart.setOnPreferenceClickListener {
      startActivity<MainIntroActivity>()
      finish()
      true
    }

    supportUs = findPreference(getString(R.string.key_support_us))
    supportUs.setOnPreferenceClickListener {
      requireContext().showInfoToast(getString(R.string.loading_ad))
      MobileAds.getRewardedVideoAdInstance(this@SettingsFragment.requireActivity()).apply {
        loadAd(BuildConfig.ADMOB_REWARDED_AD_UNIT_ID, AdRequest.Builder().addTestDevice("6E5C1B71A72DFF0228687A2FBBD676E3").build())
        rewardedVideoAdListener = object : RewardedVideoAdListener {
          override fun onRewardedVideoAdClosed() {

          }

          override fun onRewardedVideoAdLeftApplication() {

          }

          override fun onRewardedVideoAdLoaded() {
            this@apply.show()
          }

          override fun onRewardedVideoAdOpened() {

          }

          override fun onRewardedVideoCompleted() {

          }

          override fun onRewarded(p0: RewardItem?) {
            requireContext().showSuccessToast(getString(R.string.thanks))
          }

          override fun onRewardedVideoStarted() {

          }

          override fun onRewardedVideoAdFailedToLoad(p0: Int) {
            requireContext().showErrorToast(getString(R.string.failed_to_load_ad))
          }
        }
      }
      true
    }

    successTone = findPreference(getString(R.string.key_success_tone)) as RingtonePreference
    successTone.setOnPreferenceChangeListener { _, newValue ->
      requireContext().createNotifChannel(BUILD_SUCCESS_NOTIFICATION_CHANNEL_ID, "Build Success", soundUri = newValue as Uri)
      true
    }

    failureTone = findPreference(getString(R.string.key_failure_tone)) as RingtonePreference
    failureTone.setOnPreferenceChangeListener { _, newValue ->
      requireContext().createNotifChannel(BUILD_FAILURE_NOTIFICATION_CHANNEL_ID, "Build Failure", soundUri = newValue as Uri)
      true
    }
  }

  private fun updateTheme() {
    Colorful()
      .edit()
      .setPrimaryColor(themes[colors.indexOf(primaryColorValue)])
      .setAccentColor(themes[colors.indexOf(accentColorValue)])
      .setDarkTheme(isDarkTheme)
      .apply(activity!!.applicationContext) {
        activity!!.recreate()
      }
  }
}
