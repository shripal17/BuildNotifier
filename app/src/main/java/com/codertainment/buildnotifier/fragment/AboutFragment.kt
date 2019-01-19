package com.codertainment.buildnotifier.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.codertainment.buildnotifier.R
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import io.multimoon.colorful.Colorful
import org.jetbrains.anko.padding
import org.jetbrains.anko.support.v4.runOnUiThread
import us.feras.mdv.MarkdownView

class AboutFragment : MaterialAboutFragment() {
  override fun getMaterialAboutList(p0: Context): MaterialAboutList {
    val mal = MaterialAboutList.Builder()

    val card1 = MaterialAboutCard.Builder()

    card1.addItem(MaterialAboutTitleItem.Builder().apply {
      icon(R.mipmap.ic_launcher)
      text(R.string.app_name)
      desc(R.string.app_long_description)
    }.build())

    card1.addItem(
      ConvenienceBuilder.createVersionActionItem(
        requireContext(),
        getIcon(GoogleMaterial.Icon.gmd_info_outline),
        getString(R.string.about_version),
        true
      )
    )

    card1.addItem(
      MaterialAboutActionItem.Builder().apply {
        text(getString(R.string.about_open_source))
        icon(getIcon(MaterialDesignIconic.Icon.gmi_github))
        setOnClickAction {
          openGithub("https://github.com/shripal17/BuildNotifer")
        }
      }.build()
    )

    val markdownTheme = if (Colorful().getDarkTheme()) "file:///android_asset/dark.css" else "file:///android_asset/light.css"
    val changelog = MaterialAboutActionItem.Builder().apply {
      icon(getIcon(GoogleMaterial.Icon.gmd_history))
      text(getString(R.string.about_changelog))
      setOnClickAction {
        runOnUiThread {
          AlertDialog.Builder(this@AboutFragment.requireContext())
            .setPositiveButton(getString(R.string.about_changelog_close)) { d, _ ->
              d.dismiss()
            }
            .setView(MarkdownView(this@AboutFragment.requireContext()).apply {
              loadMarkdownFile("https://raw.githubusercontent.com/shripal17/BuildNotifier/master/CHANGELOG.md", markdownTheme)
              padding = 16
            })
            .create()
            .show()
        }
      }
    }
    card1.addItem(changelog.build())

    val t = if (Colorful().getDarkTheme()) Libs.ActivityStyle.DARK else Libs.ActivityStyle.LIGHT_DARK_TOOLBAR
    val licenses = MaterialAboutActionItem.Builder().apply {
      text(getString(R.string.about_open_source_licenses))
      icon(getIcon(GoogleMaterial.Icon.gmd_book))
      setOnClickAction {
        LibsBuilder()
          .withAboutIconShown(false)
          .withAboutVersionShown(false)
          .withAboutDescription("")
          .withActivityTitle(getString(R.string.about_open_source_licenses))
          .withActivityStyle(t)
          .start(this@AboutFragment.requireContext())
      }
    }
    card1.addItem(licenses.build())

    mal.addCard(card1.build())

    val people = MaterialAboutCard.Builder().apply {
      title(getString(R.string.about_people))
      addItem(getPerson("Shripal Jain", "Lead Developer", "shripal17"))
      addItem(getPerson("Savio Perera", "UI/UX Expert", "Wizper99"))
    }

    mal.addCard(people.build())

    return mal.build()
  }

  private fun getPerson(name: String, role: String, githubUsername: String? = null) = MaterialAboutActionItem.Builder().apply {
    text(name)
    subText(role)
    icon(getIcon(GoogleMaterial.Icon.gmd_person))
    if (githubUsername != null && githubUsername.isNotEmpty()) {
      setOnClickAction {
        openGithub("https://github.com/$githubUsername")
      }
    }
  }.build()

  private fun openGithub(link: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))

  private fun getIcon(iicon: IIcon) = IconicsDrawable(requireContext()).icon(iicon).color(getIconColor())

  private fun getIconColor() = Colorful().getAccentColor().getColorPack().normal().asInt()


  override fun getTheme() =
    if (Colorful().getDarkTheme()) {
      R.style.Theme_Mal_Dark_LightActionBar
    } else {
      R.style.Theme_Mal_Light_DarkActionBar
    }
}
