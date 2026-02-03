package com.achaquisse.smsgtwclient.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.achaquisse.smsgtwclient.data.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreManager = DataStoreManager(application)
    private val sessionManager = SessionManager.getInstance(application)

    var reportData by mutableStateOf<ReportResponse?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var pollingEnabled by mutableStateOf(false)
    var lastPollTime by mutableStateOf<String?>(null)

    init {
        observePollingStatus()
    }

    private fun observePollingStatus() {
        viewModelScope.launch {
            dataStoreManager.pollingEnabled.collectLatest { enabled ->
                pollingEnabled = enabled
            }
        }
        viewModelScope.launch {
            dataStoreManager.lastPollTime.distinctUntilChanged().collect { time ->
                lastPollTime = time?.let {
                    val dateTime =
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
                    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                } ?: "Never"
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val baseUrl = dataStoreManager.baseUrl.first()
                if (baseUrl.isNullOrBlank()) {
                    errorMessage = "Base URL not configured"
                    isLoading = false
                    return@launch
                }

                val now = YearMonth.now()
                val startDate = now.atDay(1).format(DateTimeFormatter.ISO_DATE)
                val endDate = now.atEndOfMonth().format(DateTimeFormatter.ISO_DATE)

                val api = NetworkModule.getApi(baseUrl, sessionManager)
                reportData = api.getReports(
                    startDate = startDate,
                    endDate = endDate,
                    aggregation = "monthly"
                )
            } catch (e: Exception) {
                errorMessage = "Failed to load reports: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
