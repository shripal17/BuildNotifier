package com.codertainment.buildnotifier.activity

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import br.tiagohm.codeview.CodeView
import br.tiagohm.codeview.Language
import br.tiagohm.codeview.Theme
import com.codertainment.buildnotifier.BuildConfig
import com.codertainment.buildnotifier.KEY_BUILD_NOTIFICATION
import com.codertainment.buildnotifier.R
import com.codertainment.buildnotifier.getThemeColor
import com.codertainment.buildnotifier.model.BuildNotification
import com.droidman.ktoasty.showErrorToast
import com.droidman.ktoasty.showInfoToast
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.mcxiaoke.koi.log.loge
import io.multimoon.colorful.CAppCompatActivity
import io.multimoon.colorful.Colorful
import kotlinx.android.synthetic.main.activity_logs.*
import kotlinx.android.synthetic.main.logs_header_view.*
import kotterknife.bindView
import org.jetbrains.anko.childrenRecursiveSequence
import org.jetbrains.anko.clipboardManager
import org.jetbrains.anko.imageResource
import java.io.File
import java.text.SimpleDateFormat

class LogsActivity : CAppCompatActivity() {

  companion object {

    fun getIntent(context: Context, notif: BuildNotification): Intent {
      val i = Intent(context, LogsActivity::class.java)
      i.putExtra(KEY_BUILD_NOTIFICATION, notif)
      return i
    }
  }

  private var notif: BuildNotification? = null

  private val icon by bindView<ImageView>(R.id.notification_icon)
  val text by bindView<TextView>(R.id.notification_text)
  val time by bindView<TextView>(R.id.notification_time)
  val device by bindView<TextView>(R.id.notification_device)
  private val progress by bindView<TextView>(R.id.notification_progress)
  private val buildVersion by bindView<TextView>(R.id.notification_build_version)
  private val timeTaken by bindView<TextView>(R.id.notification_time_taken)
  private lateinit var search: SearchView
  private lateinit var currentLogFile: File
  private var currentLog = ""
  val nameDateFormat = SimpleDateFormat("yyyyMMdd_HHmm")


  private val df = SimpleDateFormat("HH:mm\ndd/MM/yyyy")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    notif = intent.getParcelableExtra(KEY_BUILD_NOTIFICATION)
    if (notif == null) {
      loge("Got Null Notif Object")
      finish()
      return
    }

    setContentView(R.layout.activity_logs)

    setSupportActionBar(logs_toolbar)

    title = getString(R.string.title_logs)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    toolbar_layout.setExpandedTitleColor(Color.TRANSPARENT)

    icon.apply {
      imageResource = if (notif!!.status) {
        setBackgroundResource(R.drawable.background_success)
        R.drawable.ic_done_white
      } else {
        setBackgroundResource(R.drawable.background_failure)
        R.drawable.ic_clear_white
      }
    }

    text.text = if (notif!!.status) {
      getString(R.string.build_successful)
    } else {
      getString(R.string.build_failed)
    }

    time.text = df.format(notif!!.time)

    device.text = notif!!.device

    progress.text = notif!!.progress + "%"

    buildVersion.text = notif!!.buildVersion

    timeTaken.text = notif!!.formattedTimeTaken

    logs_details_container.childrenRecursiveSequence().forEach {
      if (it is TextView) {
        if (it.contentDescription != null) {
          if (it.contentDescription.isNotEmpty() && it.contentDescription.isNotBlank()) {
            TooltipCompat.setTooltipText(it, it.contentDescription)
          }
        }
      }
    }

    logs_scroll_view.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
      logs_download_fab.fabTextVisibility = if (scrollY == 0) {
        View.VISIBLE
      } else {
        View.GONE
      }
    }

    notification_log_type.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        loadLogsFor(notif!!.errorLogFile!!)
      } else {
        loadLogsFor(notif!!.logFile!!)
      }
    }

    if (notif!!.errorLogFile != null) {
      if (notif!!.errorLogFile!!.isNotBlank()) {
        notification_log_type.visibility = View.VISIBLE
        notification_log_type.isChecked = true
      } else {
        notification_log_type.visibility = View.GONE
        notification_log_type.isChecked = false
      }
    } else {
      notification_log_type.visibility = View.GONE
      notification_log_type.isChecked = false
    }

    if (notification_log_type.isChecked) {
      loadLogsFor(notif!!.errorLogFile!!)
    } else {
      loadLogsFor(notif!!.logFile!!)
    }

    logs_download_fab.setOnClickListener {
      askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
        if (!it.isAccepted) {
          showErrorToast(getString(R.string.storage_permission))
        } else {
          if (::currentLogFile.isInitialized) {
            val fileName = if (notification_log_type.isChecked) {
              "error_${nameDateFormat.format(notif!!.time)}.txt"
            } else {
              "build_${nameDateFormat.format(notif!!.time)}.txt"
            }
            val dir = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + getString(R.string.app_name) + File.separator + "Logs")
            if (!dir.exists()) {
              dir.mkdirs()
            }
            val file = File(dir.absolutePath + File.separator + fileName)
            currentLogFile.copyTo(file, overwrite = true)
            Snackbar.make(logs_root, String.format(getString(R.string.logs_saved), fileName, getString(R.string.app_name)), Snackbar.LENGTH_SHORT)
              .setActionTextColor(getThemeColor(R.attr.colorAccent))
              .setAction(R.string.open) {
                val i = Intent(Intent.ACTION_VIEW).apply {
                  data = FileProvider.getUriForFile(this@LogsActivity, BuildConfig.APPLICATION_ID + ".fileprovider", file)
                  addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivity(i)
              }
              .show()
          }
        }
      }
    }
  }

  private fun loadLogsFor(logFile: String) {
    currentLog = logFile
    logs_progress.visibility = View.VISIBLE
    logs_code_view.visibility = View.GONE
    val storageFile = FirebaseStorage.getInstance().getReference(logFile)
    currentLogFile = File.createTempFile("log", notif!!.time.toString())
    storageFile.getFile(currentLogFile).addOnSuccessListener {
      logs_download_fab.visibility = View.VISIBLE
      logs_code_view.apply {
        setOnHighlightListener(object : CodeView.OnHighlightListener {
          override fun onLanguageDetected(language: Language, relevance: Int) {

          }

          override fun onLineClicked(lineNumber: Int, content: String) {
            clipboardManager.primaryClip = ClipData.newPlainText("Build Logs", content)
            showInfoToast(String.format(getString(R.string.line_copied), lineNumber))
          }

          override fun onStartCodeHighlight() {

          }

          override fun onFontSizeChanged(p0: Int) {

          }

          override fun onFinishCodeHighlight() {
            runOnUiThread {
              logs_progress.visibility = View.GONE
              logs_code_view.visibility = View.VISIBLE
            }
          }
        })
        setCode(currentLogFile.readText())
        setShowLineNumber(true)
        setWrapLine(false)
        setZoomEnabled(true)
        setTheme(if (Colorful().getDarkTheme()) Theme.ATOM_ONE_DARK else Theme.ATOM_ONE_LIGHT)
        setLanguage(Language.ACCESS_LOG)
        apply()
      }
    }.addOnFailureListener {
      it.printStackTrace()
      showErrorToast(it.localizedMessage)
      logs_progress.visibility = View.GONE
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_logs, menu)
    search = menu!!.findItem(R.id.action_search).actionView as SearchView
    search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(p0: String?): Boolean {
        logs_code_view.findAll(p0)
        logs_code_view.toggleSearch(true)
        logs_code_view.findNext(true)
        return true
      }

      override fun onQueryTextChange(p0: String?): Boolean {
        return false
      }
    })
    search.setOnCloseListener {
      logs_code_view.toggleSearch(false)
      true
    }
    return true
  }

  private fun WebView.toggleSearch(enable: Boolean) {
    try {
      for (m in WebView::class.java.declaredMethods) {
        if (m.name == "setFindIsUp") {
          m.isAccessible = true
          m.invoke(this, enable)
          break
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    if (item?.itemId == R.id.action_next) {
      if (search.isIconified) {
        appbar_layout.setExpanded(false, true)
        logs_scroll_view.smoothScrollTo(0, logs_code_view.height)
      } else {
        logs_code_view.findNext(true)
      }
      return true
    } else if (item?.itemId == R.id.action_previous) {
      if (search.isIconified) {
        appbar_layout.setExpanded(true, true)
        logs_scroll_view.smoothScrollTo(0, 0)
      } else {
        logs_code_view.findNext(false)
      }
      return true
    }
    return false
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }
}
