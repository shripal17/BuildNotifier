package com.codertainment.buildnotifier

import android.annotation.TargetApi
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.multidex.MultiDexApplication
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.codertainment.buildnotifier.model.BuildNotification
import com.codertainment.buildnotifier.model.MyObjectBox
import com.codertainment.buildnotifier.util.PrefMan
import com.droidman.ktoasty.showInfoToast
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.mcxiaoke.koi.KoiConfig
import com.mcxiaoke.koi.ext.find
import com.mcxiaoke.koi.log.logd
import com.mcxiaoke.koi.log.logi
import com.mcxiaoke.koi.utils.oreoOrNewer
import io.multimoon.colorful.Defaults
import io.multimoon.colorful.ThemeColor
import io.multimoon.colorful.initColorful
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser
import io.objectbox.kotlin.boxFor
import org.jetbrains.anko.clipboardManager
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
  }
}

val Context.boxStore
  get() = (applicationContext as BaseApp).boxStore

val Context.notifBox
  get() = boxStore.boxFor<BuildNotification>()

fun Context.getPrefs(): PrefMan = PrefMan.getInstance(this)

const val BUILD_SUCCESS_NOTIFICATION_CHANNEL_ID = "build_success"

const val BUILD_FAILURE_NOTIFICATION_CHANNEL_ID = "build_failure"

const val ACTION_ON_NOTIFICATION_RECEIVED = "${BuildConfig.APPLICATION_ID}.notification.received"

const val KEY_BUILD_NOTIFICATION = "build_notification"

const val KEY_FCM_TOKEN = "fcm_token"

const val KEY_QUICK_START = "quick_start"

fun Activity.getThemeColor(colorRef: Int) = TypedValue().also {
  theme.resolveAttribute(colorRef, it, true)
}.data

@TargetApi(Build.VERSION_CODES.O)
fun Context.createNotifChannel(channelId: String, title: String, groupId: String? = null, soundUri: Uri? = null) {
  if (oreoOrNewer()) {
    if (notificationManager.getNotificationChannel(channelId) != null) {
      notificationManager.deleteNotificationChannel(channelId)
    }
    val notificationChannel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_HIGH).apply {
      enableLights(true)
      lightColor = Color.BLUE
      soundUri?.let {
        logd("SoundUri:", soundUri.toString())
        setSound(
          soundUri,
          AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
        )
      }
      enableVibration(true)
      if (groupId != null) {
        group = groupId
      }
    }
    notificationManager.createNotificationChannel(notificationChannel)
  }
}

fun Context.showDeviceTokenDialog() {
  val v = LayoutInflater.from(this).inflate(R.layout.dialog_fcm_token, null, false)
  val fcmText = v.find<TextView>(R.id.fcm_token)
  val fcmTextCopy = v.find<ImageButton>(R.id.fcm_token_copy)

  val fcmToken = FirebaseInstanceId.getInstance().token
  fcmToken?.let {
    logd("fcmToken", it.length.toString())
  }

  fcmTextCopy.setOnClickListener {
    clipboardManager.primaryClip = ClipData.newPlainText(getString(R.string.settings_device_token), fcmToken)
    showInfoToast(getString(R.string.settings_device_token_copied))
  }

  fcmText.text = getString(R.string.settings_device_token_text, fcmToken)

  AlertDialog.Builder(this)
    .setTitle(R.string.title_fcm_token)
    .setView(v)
    .setPositiveButton(android.R.string.ok) { d, _ ->
      d.dismiss()
    }
    .create()
    .show()
}

const val REPO_OWNER = "shripal17"
const val APP_REPO_NAME = "BuildNotifier"
const val BUILDER_REPO_NAME = "BuildNotifierServer"
const val APP_GITHUB_LINK = "https://github.com/$REPO_OWNER/$APP_REPO_NAME"
const val BUILDER_GITHUB_LINK = "https://github.com/$REPO_OWNER/$BUILDER_REPO_NAME"