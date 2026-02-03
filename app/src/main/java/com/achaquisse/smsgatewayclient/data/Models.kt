package com.achaquisse.smsgatewayclient.data

import com.google.gson.annotations.SerializedName

data class Message(
    val id: String,
    @SerializedName("to_number") val toNumber: String,
    val body: String
)

data class PollResponse(
    val messages: List<Message>
)

data class StatusUpdateRequest(
    val status: String,
    val reason: String? = null
)

data class DeviceConfigRequest(
    val topics: List<String>
)

data class DeviceConfigResponse(
    val topics: List<String>
)

data class ReportResponse(
    val summary: ReportSummary,
    @SerializedName("by_topic") val byTopic: List<TopicStats>
) {
    data class ReportSummary(
        val total: Int,
        val sent: Int,
        val failed: Int
    )

    data class TopicStats(
        val topic: String,
        val total: Int
    )
}
