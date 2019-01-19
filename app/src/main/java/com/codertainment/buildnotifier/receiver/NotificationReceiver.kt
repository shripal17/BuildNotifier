package com.codertainment.buildnotifier.receiver

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.codertainment.buildnotifier.*
import com.codertainment.buildnotifier.activity.LogsActivity
import com.codertainment.buildnotifier.activity.MainActivity
import com.codertainment.buildnotifier.model.BuildNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mcxiaoke.koi.log.logd
import com.mcxiaoke.koi.log.loge
import org.jetbrains.anko.notificationManager

class NotificationReceiver : FirebaseMessagingService() {

  override fun onMessageReceived(message: RemoteMessage?) {
    if (message == null) {
      loge("null Remote Message Received")
      return
    }

    val data = message.data
    if (data == null) {
      loge("Notification has no data")
      loge(message.toString())
      return
    }

    val notif = BuildNotification(
      status = data["status"]!!.toBoolean(),
      time = data["time"]!!.toLong(),
      logFile = data["logFile"],
      currentStep = data["currentStep"],
      buildVersion = data["buildVersion"],
      device = data["device"],
      timeTaken = data["timeTaken"]!!.toLong()
    )

    notifBox.put(notif)

    val title = if (notif.status) {
      getString(R.string.build_successful)
    } else {
      getString(R.string.build_failed)
    }

    val openMain = Intent(this, MainActivity::class.java)
    val openMainPi = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), openMain, PendingIntent.FLAG_ONE_SHOT)

    val icon = if (notif.status) {
      R.drawable.ic_done_white
    } else {
      R.drawable.ic_clear_white
    }

    logd("notifParams", notif.device + notif.currentStep + notif.formattedTimeTaken)

    val notifBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
      .setContentTitle(title)
      .setWhen(notif.time)
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText(
            String.format(
              getString(R.string.notification_text),
              notif.device, notif.currentStep, notif.formattedTimeTaken
            )
          )
      )
      .setContentIntent(openMainPi)
      .setAutoCancel(true)
      .setSmallIcon(icon)


    if (notif.logFile != null && notif.logFile != "") {
      val viewLogsIntent = LogsActivity.getIntent(this, notif)
      val viewLogsPendingIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), viewLogsIntent, PendingIntent.FLAG_ONE_SHOT)
      notifBuilder.addAction(R.drawable.ic_view_logs_black, getString(R.string.notification_action_view_logs), viewLogsPendingIntent)
    }

    notificationManager.notify(System.currentTimeMillis().toInt(), notifBuilder.build())

    val broadcastIntent = Intent(ACTION_ON_NOTIFICATION_RECEIVED).apply {
      setClass(this@NotificationReceiver, MainActivity.NotificationListener::class.java)
    }
    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
  }

  override fun onNewToken(p0: String?) {
    if (p0 != null) {
      getPrefs().saveString(KEY_FCM_TOKEN, p0)
    }
  }
}