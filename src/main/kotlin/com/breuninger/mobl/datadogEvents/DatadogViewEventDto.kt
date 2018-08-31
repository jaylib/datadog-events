package com.breuninger.mobl.datadogEvents

import com.google.gson.annotations.SerializedName

import java.time.Instant
import java.util.ArrayList

class DatadogViewEventDto {
    var id: String? = null

    @SerializedName("related_event_id")
    var relatedEventId: String? = null

    var happend: Instant? = null
    var handle: String? = null
    var title: String? = null
    var text: String? = null
    var priority: DatadogEventPriority? = null
    var tags: List<String> = ArrayList()

    protected fun canEqual(other: Any): Boolean {
        return other is DatadogViewEventDto
    }
}
