package com.codertainment.buildnotifier.fragment

import android.content.ClipData
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.codertainment.buildnotifier.BuildConfig
import com.codertainment.buildnotifier.KEY_FCM_TOKEN
import com.codertainment.buildnotifier.R
import com.codertainment.buildnotifier.activity.MainIntroActivity
import com.codertainment.buildnotifier.getPrefs
import com.droidman.ktoasty.showInfoToast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.mcxiaoke.koi.ext.find
import com.mcxiaoke.koi.ext.finish
import com.mcxiaoke.koi.log.logd
import com.takisoft.preferencex.ColorPickerPreference
import com.takisoft.preferencex.PreferenceFragmentCompat
import io.multimoon.colorful.Colorful
import io.multimoon.colorful.ThemeColor
import org.jetbrains.anko.clipboardManager
import org.jetbrains.anko.support.v4.startActivity

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

  private lateinit var primaryColor: ColorPickerPreference
  private lateinit var accentColor: ColorPickerPreference
  private lateinit var darkTheme: SwitchPreference
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
        val v = layoutInflater.inflate(R.layout.dialog_fcm_token, null, false)
        val fcmText = v.find<TextView>(R.id.fcm_token)
        val fcmTextCopy = v.find<ImageButton>(R.id.fcm_token_copy)

        val fcmToken = this@SettingsFragment.requireContext().getPrefs().getString(KEY_FCM_TOKEN)
        fcmToken?.let {
          logd("fcmToken", it.length.toString())
        }

        fcmTextCopy.setOnClickListener {
          this@SettingsFragment.requireContext().clipboardManager.primaryClip = ClipData.newPlainText(getString(R.string.settings_device_token), fcmToken)
          this@SettingsFragment.requireContext().showInfoToast(getString(R.string.settings_device_token_copied))
        }

        fcmText.text = getString(R.string.settings_device_token_text, fcmToken)

        AlertDialog.Builder(this@SettingsFragment.requireContext())
          .setTitle(R.string.title_fcm_token)
          .setView(v)
          .setPositiveButton(android.R.string.ok) { d, _ ->
            d.dismiss()
          }
          .create()
          .show()
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
        loadAd(BuildConfig.ADMOB_REWARDED_AD_UNIT_ID, AdRequest.Builder().addTestDevice("3A3EAD8011A8331155F75F36592A8315").build())
        if (isLoaded) {
          show()
        }
      }
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
