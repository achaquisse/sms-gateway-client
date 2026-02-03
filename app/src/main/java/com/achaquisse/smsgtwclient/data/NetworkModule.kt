package com.achaquisse.smsgtwclient.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private var api: GatewayApi? = null
    private var currentBaseUrl: String? = null

    fun getApi(baseUrl: String, sessionManager: SessionManager): GatewayApi {
        if (api == null || currentBaseUrl != baseUrl) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager))
                .addInterceptor(logging)
                .build()

            api = Retrofit.Builder()
                .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(GatewayApi::class.java)
            currentBaseUrl = baseUrl
        }
        return api!!
    }
}
