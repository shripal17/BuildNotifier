package com.codertainment.buildnotifier

import android.annotation.TargetApi
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.util.TypedValue
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.multidex.MultiDexApplication
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.codertainment.buildnotifier.model.BuildNotification
import com.codertainment.buildnotifier.model.MyObjectBox
import com.codertainment.buildnotifier.util.PrefMan
import com.google.firebase.messaging.FirebaseMessaging
import com.mcxiaoke.koi.KoiConfig
import com.mcxiaoke.koi.log.logi
import com.mcxiaoke.koi.utils.oreoOrNewer
import io.multimoon.colorful.Defaults
import io.multimoon.colorful.ThemeColor
import io.multimoon.colorful.initColorful
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser
import io.objectbox.kotlin.boxFor
import org.jetbrains.anko.notificationManager

class BaseApp : MultiDexApplication() {

  lateinit var boxStore: BoxStore

  override fun onCreate() {
    super.onCreate()

    val defaults = Defaults(
      primaryColor = ThemeColor.DEEP_PURPLE,
      accentColor = ThemeColor.DEEP_ORANGE,
      useDarkTheme = true,
      translucent = false
    )
    initColorful(this, defaults)

    AndroidNetworking.initialize(this)

    FirebaseMessaging.getInstance().subscribeToTopic("default")

    boxStore = MyObjectBox.builder().androidContext(this).build()

    if (BuildConfig.DEBUG) {
      KoiConfig.logEnabled = true
      KoiConfig.logLevel = Log.DEBUG
      val started = AndroidObjectBrowser(boxStore).start(this)
      AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.BODY)
      logi("ObjectBrowser", "Started: $started")
    }

    createNotifChannel(NOTIFICATION_CHANNEL_ID, "Build Notifications")
  }


  @TargetApi(Build.VERSION_CODES.O)
  fun createNotifChannel(channelId: String, title: String, groupId: String? = null) {
    if (oreoOrNewer()) {
      val notificationChannel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_HIGH).apply {
        enableLights(true)
        lightColor = Color.BLUE
        enableVibration(true)
        if (groupId != null) {
          group = groupId
        }
      }
      notificationManager.createNotificationChannel(notificationChannel)
    }
  }
}

val Context.boxStore
  get() = (applicationContext as BaseApp).boxStore

val Context.notifBox
  get() = boxStore.boxFor<BuildNotification>()

fun Context.getPrefs(): PrefMan = PrefMan.getInstance(this)

const val NOTIFICATION_CHANNEL_ID = "notification_channel"

const val ACTION_ON_NOTIFICATION_RECEIVED = "${BuildConfig.APPLICATION_ID}.notification.received"

const val KEY_BUILD_NOTIFICATION = "build_notification"

const val KEY_FCM_TOKEN = "fcm_token"

const val KEY_QUICK_START = "quick_start"

fun Activity.getThemeColor(colorRef: Int) = TypedValue().also {
  theme.resolveAttribute(colorRef, it, true)
}.data
