package com.breuninger.mobl.datadogEvents

data class DatadogEventText(private val text: String) {

    init {
        if (text.length > 4000) {
            throw IllegalArgumentException("Text is too long. Limit is 4000 characters.")
        }
    }

    override fun toString(): String {
        return text
    }
}
