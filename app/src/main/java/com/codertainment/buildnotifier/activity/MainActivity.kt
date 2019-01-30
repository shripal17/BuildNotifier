package com.codertainment.buildnotifier.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.GravityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.codertainment.buildnotifier.*
import com.codertainment.buildnotifier.adapter.NotificationAdapter
import com.codertainment.buildnotifier.helper.RevealCircleAnimatorHelper
import com.codertainment.buildnotifier.helper.SimpleItemTouchHelperCallback
import com.droidman.ktoasty.showErrorToast
import com.droidman.ktoasty.showInfoToast
import com.droidman.ktoasty.showSuccessToast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.material.navigation.NavigationView
import com.mcxiaoke.koi.ext.delayed
import com.mcxiaoke.koi.ext.startActivity
import io.multimoon.colorful.CAppCompatActivity
import io.multimoon.colorful.Colorful
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : CAppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

  private lateinit var adapter: NotificationAdapter
  private lateinit var callback: SimpleItemTouchHelperCallback
  private lateinit var itemTouchHelper: ItemTouchHelper
  private lateinit var search: SearchView
  private var selectedSortMethod = 1
  private var reverseSort = true

  private val notificationListener = NotificationListener()

  companion object {
    fun newIntent(context: Context, sourceView: View? = null): Intent {
      return Intent(context, MainActivity::class.java).also {
        RevealCircleAnimatorHelper.addBundleValues(it, sourceView)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    RevealCircleAnimatorHelper
      .create(this)
      .start(main_root, Colorful().getPrimaryColor().getColorPack().normal().asInt(), getThemeColor(android.R.attr.colorBackground))

    MobileAds.initialize(this, BuildConfig.ADMOB_APP_ID)

    setSupportActionBar(main_toolbar)

    title = getString(R.string.main_title_notifications)

    val toggle = ActionBarDrawerToggle(this, main_drawer_layout, main_toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
    main_drawer_layout.addDrawerListener(toggle)
    toggle.syncState()

    main_nav_view.apply {
      setNavigationItemSelectedListener(this@MainActivity)
      setCheckedItem(R.id.nav_notifications)
    }

    refresh()

    main_adView.loadAd(AdRequest.Builder().addTestDevice("6E5C1B71A72DFF0228687A2FBBD676E3").build())
    main_adView.adListener = object : AdListener() {
      override fun onAdLoaded() {
        super.onAdLoaded()
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, main_adView.measuredHeight)
        main_recycler.layoutParams = lp
      }
    }
  }

  override fun onResume() {
    super.onResume()
    LocalBroadcastManager.getInstance(this).registerReceiver(notificationListener, IntentFilter(ACTION_ON_NOTIFICATION_RECEIVED))
  }

  override fun onPause() {
    super.onPause()
    LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationListener)
  }

  fun refresh() {
    val notifs = notifBox.all

    if (notifs.isEmpty()) {
      main_no_notifications_container.visibility = View.VISIBLE
      main_recycler.invalidate()
      return
    } else {
      main_no_notifications_container.visibility = View.GONE
    }

    when (selectedSortMethod) {
      0 -> notifs.sortBy { it.status }
      1 -> notifs.sortBy { it.time }
      2 -> notifs.sortBy { it.device }
      3 -> notifs.sortBy { it.buildVersion }
      4 -> notifs.sortBy { it.timeTaken }
      5 -> notifs.sortBy { it.progress }
    }

    if (reverseSort) {
      notifs.reverse()
    }

    main_recycler.apply {
      layoutManager = LinearLayoutManager(this@MainActivity)
      isNestedScrollingEnabled = false
      invalidate()
    }

    adapter = NotificationAdapter(this, ArrayList(notifs))

    callback = SimpleItemTouchHelperCallback(adapter)
    itemTouchHelper = ItemTouchHelper(callback)
    itemTouchHelper.attachToRecyclerView(main_recycler)

    main_recycler.adapter = adapter
  }

  inner class NotificationListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (context != null && intent != null && intent.action == ACTION_ON_NOTIFICATION_RECEIVED && main_recycler != null) {
        refresh()
      }
    }
  }

  override fun onNavigationItemSelected(p0: MenuItem): Boolean {
    when (p0.itemId) {
      R.id.nav_build_script_setup -> {
        CustomTabsIntent.Builder().apply {
          setToolbarColor(Colorful().getPrimaryColor().getColorPack().normal().asInt())
          build().launchUrl(this@MainActivity, Uri.parse("https://github.com/shripal17/BuildNotifierServer/blob/master/README.md"))
        }
        return true
      }
      R.id.nav_support_us -> {
        MaterialDialog(this).show {
          message(R.string.support_us_warning)
          positiveButton(android.R.string.ok) {
            showRewardedVideoAd()
          }
          negativeButton(android.R.string.cancel)
        }
      }
      R.id.nav_device_token -> {
        showDeviceTokenDialog()
      }
      R.id.nav_settings -> {
        startActivity<SettingsActivity>()
      }
      R.id.nav_review_us -> {
        val i = Intent(Intent.ACTION_VIEW).apply {
          data = Uri.parse("http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
        }
        startActivity(i)
      }
      R.id.nav_other_apps -> {
        val i = Intent(Intent.ACTION_VIEW).apply {
          data = Uri.parse("http://play.google.com/store/apps/dev?id=7841103242304150967")
        }
        startActivity(i)
      }
      R.id.nav_about -> {
        startActivity<AboutActivity>()
      }
    }
    main_nav_view.setCheckedItem(R.id.nav_notifications)
    main_drawer_layout.closeDrawers()
    return true
  }

  private fun showRewardedVideoAd() {
    showInfoToast(getString(R.string.loading_ad))
    MobileAds.getRewardedVideoAdInstance(this).apply {
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
          showSuccessToast(getString(R.string.thanks))
        }

        override fun onRewardedVideoStarted() {

        }

        override fun onRewardedVideoAdFailedToLoad(p0: Int) {
          showErrorToast(getString(R.string.failed_to_load_ad))
        }
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    search = menu!!.findItem(R.id.action_search).actionView as SearchView
    search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(p0: String?): Boolean {
        if (::adapter.isInitialized) {
          adapter.filter.filter(p0)
        }
        return true
      }

      override fun onQueryTextChange(p0: String?): Boolean {
        if (::adapter.isInitialized) {
          adapter.filter.filter(p0)
        }
        return true
      }
    })
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    if (item?.itemId == R.id.action_sort) {
      MaterialDialog(this).show {
        title(R.string.main_sort_by)
        checkBoxPrompt(R.string.main_sort_reversed, isCheckedDefault = reverseSort) {
          reverseSort = it
        }
        listItemsSingleChoice(R.array.sort_by_fields, initialSelection = selectedSortMethod) { _, i, _ ->
          selectedSortMethod = i
        }
        positiveButton(R.string.abc_action_mode_done) {
          Handler().delayed(200) {
            refresh()
          }
        }
        negativeButton(android.R.string.cancel) {
          it.dismiss()
        }
      }
      return true
    } else if (item?.itemId == R.id.action_delete_all) {
      MaterialDialog(this).show {
        title(R.string.warning)
        message(R.string.delete_all_warning)
        positiveButton(R.string.i_know_what_i_am_doing) {
          notifBox.removeAll()
          Handler().delayed(500) {
            refresh()
          }
        }
        negativeButton(R.string.nevermind)
      }
    }
    return false
  }

  override fun onBackPressed() {
    if (main_drawer_layout.isDrawerOpen(GravityCompat.START)) {
      main_drawer_layout.closeDrawers()
    } else {
      super.onBackPressed()
    }
  }
}
