package com.codertainment.buildnotifier.model

import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.android.parcel.Parcelize
import java.util.concurrent.TimeUnit

@Parcelize
@Entity
data class BuildNotification(
  @Id var id: Long = 0L,
  var status: Boolean = false,
  var time: Long = System.currentTimeMillis(),
  var logFile: String? = "",
  var currentStep: String? = "",
  var buildVersion: String? = "",
  var device: String? = "",
  var timeTaken: Long = 0L
) : Parcelable {

  val formattedTimeTaken
  get() = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeTaken),
                        TimeUnit.MILLISECONDS.toMinutes(timeTaken) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(timeTaken) % TimeUnit.MINUTES.toSeconds(1))
}