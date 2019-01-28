package com.codertainment.buildnotifier.adapter

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.TooltipCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.codertainment.buildnotifier.KEY_BUILD_NOTIFICATION
import com.codertainment.buildnotifier.R
import com.codertainment.buildnotifier.activity.LogsActivity
import com.codertainment.buildnotifier.activity.MainActivity
import com.codertainment.buildnotifier.getThemeColor
import com.codertainment.buildnotifier.helper.ItemTouchHelperAdapter
import com.codertainment.buildnotifier.model.BuildNotification
import com.codertainment.buildnotifier.notifBox
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotterknife.bindView
import org.jetbrains.anko.childrenRecursiveSequence
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.vibrator
import java.text.SimpleDateFormat

class NotificationAdapter(val ctx: Activity, private val allData: List<BuildNotification>) :
  RecyclerView.Adapter<NotificationAdapter.BuildNotificationViewHolder>(), ItemTouchHelperAdapter, Filterable {

  val df = SimpleDateFormat("HH:mm\ndd/MM/yyyy")
  var data = ArrayList(allData)

  override fun onItemDismiss(position: Int) {
    val item = data[position]
    var remove = true

    data.removeAt(position)
    notifyItemRemoved(position)

    if (ctx.vibrator.hasVibrator()) {
      ctx.vibrator.vibrate(100)
    }

    Snackbar.make((ctx as MainActivity).main_root, ctx.getString(R.string.deleted_notification, item.device), Snackbar.LENGTH_LONG)
      .setActionTextColor(ctx.getThemeColor(R.attr.colorAccent))
      .setAction("UNDO") {
        remove = false
        data.add(position, item)
        notifyItemInserted(position)
      }
      .addCallback(object : Snackbar.Callback() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
          if (remove) {
            ctx.notifBox.remove(item.id)
          }
        }
      })
      .show()
  }

  override fun getFilter(): Filter = object : Filter() {
    override fun performFiltering(p0: CharSequence?): FilterResults {
      var filtered = ArrayList<BuildNotification>()

      p0?.let {
        val toFilter = p0.toString().toLowerCase()

        filtered = if (toFilter.isEmpty() || p0.isBlank()) {
          ArrayList(allData)
        } else {
          ArrayList(
            allData.filter {
              val device = if (it.device != null) it.device!!.toLowerCase() else ""
              val currentStep = if (it.progress != null) it.progress!!.toLowerCase() else ""
              val buildVersion = if (it.buildVersion != null) it.buildVersion!!.toLowerCase() else ""
              device.contains(toFilter) || currentStep.contains(toFilter) || buildVersion.contains(toFilter)
            }
          )
        }
      }
      val results = FilterResults()
      results.values = filtered
      return results
    }

    override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
      p1?.let {
        data = p1.values as ArrayList<BuildNotification>
        notifyDataSetChanged()
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    BuildNotificationViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_notification, parent, false))

  override fun getItemCount() = data.size

  override fun onBindViewHolder(holder: BuildNotificationViewHolder, position: Int) = holder.bind(data[holder.adapterPosition])

  inner class BuildNotificationViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    private val detailsRoot by bindView<ConstraintLayout>(R.id.details_root)
    private val card by bindView<androidx.cardview.widget.CardView>(R.id.notification_card)
    private val icon by bindView<ImageView>(R.id.notification_icon)
    val text by bindView<TextView>(R.id.notification_text)
    val time by bindView<TextView>(R.id.notification_time)
    val device by bindView<TextView>(R.id.notification_device)
    private val progress by bindView<TextView>(R.id.notification_progress)
    private val buildVersion by bindView<TextView>(R.id.notification_build_version)
    private val timeTaken by bindView<TextView>(R.id.notification_time_taken)

    fun bind(item: BuildNotification) {
      icon.apply {
        imageResource = if (item.status) {
          setBackgroundResource(R.drawable.background_success)
          R.drawable.ic_done_white
        } else {
          setBackgroundResource(R.drawable.background_failure)
          R.drawable.ic_clear_white
        }
      }

      text.text = if (item.status) {
        ctx.getString(R.string.build_successful)
      } else {
        ctx.getString(R.string.build_failed)
      }

      time.text = df.format(item.time)

      device.text = item.device

      progress.text = item.progress + "%"

      buildVersion.text = item.buildVersion

      timeTaken.text = item.formattedTimeTaken

      card.childrenRecursiveSequence().forEach {
        if (it is TextView) {
          if (it.contentDescription != null) {
            if (it.contentDescription.isNotEmpty() && it.contentDescription.isNotBlank()) {
              TooltipCompat.setTooltipText(it, it.contentDescription)
            }
          }
        }
      }

      card.setOnClickListener {
        val openLogs = Intent(ctx, LogsActivity::class.java)
        openLogs.putExtra(KEY_BUILD_NOTIFICATION, item)
        val options = ActivityOptions.makeSceneTransitionAnimation(ctx, detailsRoot, "details")
        ctx.startActivity(openLogs, options.toBundle())
      }
    }
  }
}