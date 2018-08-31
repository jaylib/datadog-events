package com.breuninger.mobl.datadogEvents

/**
 * Created by fmielke on 10.07.17.
 */

enum class DatadogEventAlertType private constructor(internal var priority: String) {
  Error("error"),
  Warning("warning"),
  Success("success"),
  Info("info");

  override fun toString(): String {
    return priority
  }
}
