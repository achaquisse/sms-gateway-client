package com.achaquisse.smsgatewayclient.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GatewayApi {
    @GET("devices")
    suspend fun getDeviceConfig(): DeviceConfigResponse

    @PUT("devices")
    suspend fun updateDeviceConfig(@Body request: DeviceConfigRequest)

    @GET("gateway/poll")
    suspend fun pollMessages(): PollResponse

    @PUT("gateway/status/{messageId}")
    suspend fun updateStatus(
        @Path("messageId") messageId: String,
        @Body request: StatusUpdateRequest
    )

    @GET("reports")
    suspend fun getReports(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("aggregation") aggregation: String
    ): ReportResponse
}
