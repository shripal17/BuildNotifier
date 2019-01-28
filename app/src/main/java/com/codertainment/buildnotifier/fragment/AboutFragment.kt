package com.codertainment.buildnotifier.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.codertainment.buildnotifier.APP_GITHUB_LINK
import com.codertainment.buildnotifier.APP_REPO_NAME
import com.codertainment.buildnotifier.R
import com.codertainment.buildnotifier.REPO_OWNER
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.community_material_typeface_library.CommunityMaterial
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
          openLink(APP_GITHUB_LINK)
        }
      }.build()
    )

    val markdownTheme = if (Colorful().getDarkTheme()) "file:///android_asset/markdown/dark.css" else "file:///android_asset/markdown/light.css"
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
              loadMarkdownFile("https://raw.githubusercontent.com/$REPO_OWNER/$APP_REPO_NAME/master/CHANGELOG.md", markdownTheme)
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
      addItem(getPerson("Shripal Jain", getString(R.string.lead_developer), REPO_OWNER))
      addItem(getPerson("Pranay Narang", getString(R.string.translations_manager), "TheDarkBeast"))
      addItem(getPerson("Savio Perera", getString(R.string.ui_ux_expert), "Wizper99"))
    }

    mal.addCard(people.build())

    val social = MaterialAboutCard.Builder().title(R.string.contact)

    val telegram = MaterialAboutActionItem.Builder().apply {
      text(R.string.telegram)
      icon(getIcon(CommunityMaterial.Icon2.cmd_telegram))
      setOnClickAction {
        openLink("https://t.me/BuildNotifier")
      }
    }
    social.addItem(telegram.build())

    val xda = MaterialAboutActionItem.Builder().apply {
      text(R.string.xda_thread)
      icon(getIcon(CommunityMaterial.Icon2.cmd_xda))
      setOnClickAction {
        // TODO Replace with actual XDA Thread Link
        openLink("https://xda-developers.com")
      }
    }
    social.addItem(xda.build())

    mal.addCard(social.build())

    return mal.build()
  }

  private fun getPerson(name: String, role: String, githubUsername: String? = null) = MaterialAboutActionItem.Builder().apply {
    text(name)
    subText(role)
    icon(getIcon(GoogleMaterial.Icon.gmd_person))
    if (githubUsername != null && githubUsername.isNotEmpty()) {
      setOnClickAction {
        openLink("https://github.com/$githubUsername")
      }
    }
  }.build()

  private fun openLink(link: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))

  private fun getIcon(iicon: IIcon) = IconicsDrawable(requireContext()).icon(iicon).color(getIconColor())

  private fun getIconColor() = Colorful().getAccentColor().getColorPack().normal().asInt()


  override fun getTheme() =
    if (Colorful().getDarkTheme()) {
      R.style.Theme_Mal_Dark_LightActionBar
    } else {
      R.style.Theme_Mal_Light_DarkActionBar
    }
}
