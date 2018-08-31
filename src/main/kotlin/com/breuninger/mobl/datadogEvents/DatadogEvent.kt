package com.breuninger.mobl.datadogEvents

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.util.*

class DatadogEvent(title: String, text: String, private val priority: DatadogEventPriority, @SerializedName("alert_type")
private val alertType: DatadogEventAlertType, additionalTags: List<String> = listOf()) {
  private val title: String = DatadogEventTitle(title).toString()
  private val text: String = DatadogEventText(text).toString()

  private val tags = ArrayList<String>()

  init {
    this.tags.addAll(additionalTags)
  }

  fun setTags(tags: List<String>) {
    this.tags.addAll(tags)
  }

  fun deflate(): String {
    return gson.toJson(this)
  }

  companion object {
    private val gson = GsonBuilder().create()
  }
}
