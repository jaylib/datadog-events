package com.breuninger.mobl.datadogEvents

data class DatadogResponseDto(var event: DatadogViewEventDto? = null, var status: String? = null) {

    protected fun canEqual(other: Any): Boolean {
        return other is DatadogResponseDto
    }

    override fun toString(): String {
        return "DataDogResponseDto(event=" + this.event + ", status=" + this.status + ")"
    }
}
