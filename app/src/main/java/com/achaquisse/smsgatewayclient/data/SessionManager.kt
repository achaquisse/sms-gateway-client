package com.achaquisse.smsgatewayclient.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    fun getDeviceKey(): String? {
        return prefs.getString(KEY_DEVICE_KEY, null)
    }

    fun saveDeviceKey(key: String) {
        prefs.edit().putString(KEY_DEVICE_KEY, key).apply()
    }

    companion object {
        private const val KEY_DEVICE_KEY = "device_key"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
