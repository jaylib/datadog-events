package com.breuninger.mobl.datadogEvents

data class DatadogEventAggregationKey(private val key: String) {
    init {
        if (key.length > 100) {
            throw IllegalArgumentException("Given key is too long. Maximum length is 100.")
        }
    }
    override fun toString(): String {
        return key
    }
}
