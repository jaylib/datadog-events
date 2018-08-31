package com.breuninger.mobl.datadogEvents

enum class DatadogEventPriority private constructor(private val priority: String) {
  Normal("normal"),
  Low("low");

  override fun toString(): String {
    return priority
  }
}
