package com.achaquisse.smsgatewayclient.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.achaquisse.smsgatewayclient.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConfigViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreManager = DataStoreManager(application)
    private val sessionManager = SessionManager.getInstance(application)

    var baseUrl by mutableStateOf("")
    var deviceKey by mutableStateOf("")
    var pollingEnabled by mutableStateOf(false)
    val topics = mutableStateListOf<String>()
    
    var snackbarMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    init {
        loadSettingsAndFetchConfig()
    }

    private fun loadSettingsAndFetchConfig() {
        viewModelScope.launch {
            baseUrl = dataStoreManager.baseUrl.first() ?: ""
            deviceKey = dataStoreManager.deviceKey.first() ?: ""
            pollingEnabled = dataStoreManager.pollingEnabled.first()
            
            if (baseUrl.isNotBlank() && deviceKey.isNotBlank()) {
                fetchConfig()
            }
        }
    }

    fun fetchConfig() {
        viewModelScope.launch {
            isLoading = true
            try {
                sessionManager.saveDeviceKey(deviceKey)
                val api = NetworkModule.getApi(baseUrl, sessionManager)
                val config = api.getDeviceConfig()
                topics.clear()
                topics.addAll(config.topics)
            } catch (e: Exception) {
                snackbarMessage = "Error fetching config: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun addTopic(topic: String) {
        if (topic.isNotBlank() && !topics.contains(topic)) {
            topics.add(topic)
        }
    }

    fun removeTopic(topic: String) {
        topics.remove(topic)
    }

    fun togglePolling(enabled: Boolean) {
        viewModelScope.launch {
            pollingEnabled = enabled
            dataStoreManager.setPollingEnabled(enabled)
        }
    }

    fun saveAndSync() {
        viewModelScope.launch {
            try {
                // Save to DataStore
                dataStoreManager.saveSettings(baseUrl, deviceKey)
                // Update SessionManager for Interceptor
                sessionManager.saveDeviceKey(deviceKey)

                // Call API
                val api = NetworkModule.getApi(baseUrl, sessionManager)
                api.updateDeviceConfig(DeviceConfigRequest(topics.toList()))
                
                snackbarMessage = "Configuration saved and synced successfully"
            } catch (e: Exception) {
                snackbarMessage = "Error: ${e.message}"
            }
        }
    }
    
    fun snackbarShown() {
        snackbarMessage = null
    }
}
