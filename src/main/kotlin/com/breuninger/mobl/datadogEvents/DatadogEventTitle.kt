package com.breuninger.mobl.datadogEvents

data class DatadogEventTitle(private val title: String) {

    init {
        if (title.length > 100) {
            throw IllegalArgumentException("title is too long. Limit is 100 characters")
        }
    }

    override fun toString(): String {
        return title
    }
}
