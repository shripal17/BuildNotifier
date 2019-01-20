package com.codertainment.buildnotifier.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codertainment.buildnotifier.R
import kotterknife.bindView

class CodeLineAdapter(val ctx: Context, val data: List<String>) : RecyclerView.Adapter<CodeLineAdapter.StringViewHolder>() {

  val spannables = arrayOf(
    Pair("error:", ContextCompat.getColor(ctx, com.codertainment.buildnotifier.R.color.md_red_700)),
    Pair("warning:", ContextCompat.getColor(ctx, R.color.md_deep_orange_700)),
    Pair("note:", ContextCompat.getColor(ctx, R.color.md_blue_700))
  )

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StringViewHolder(LayoutInflater.from(ctx).inflate(R.layout.item_code_line, parent, false))

  override fun getItemCount() = data.size

  override fun onBindViewHolder(holder: StringViewHolder, position: Int) = holder.bind(data[holder.adapterPosition])

  inner class StringViewHolder(val v: View) : RecyclerView.ViewHolder(v) {

    val lineNumber by bindView<TextView>(R.id.code_line_number)
    val lineText by bindView<TextView>(R.id.code_line_text)

    fun bind(item: String) {
      lineNumber.text = (adapterPosition + 1).toString()
      val spannableString = SpannableString(item)
      spannables.forEach { spannable ->
        if (item.contains(spannable.first)) {
          val regex = Regex(spannable.first)
          val occurrences = regex.findAll(item)
          occurrences.forEach {
            spannableString.setSpan(
              ForegroundColorSpan(spannable.second),
              it.range.first, it.range.last,
              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
          }
        }
      }
      lineText.setText(item, TextView.BufferType.SPANNABLE)

    }
  }
}