package com.achaquisse.smsgatewayclient.data

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val deviceKey = sessionManager.getDeviceKey()

        val requestBuilder = originalRequest.newBuilder()
        if (deviceKey != null) {
            requestBuilder.header("X-Device-Key", deviceKey)
        }

        return chain.proceed(requestBuilder.build())
    }
}
