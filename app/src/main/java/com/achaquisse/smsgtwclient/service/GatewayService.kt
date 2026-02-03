package com.achaquisse.smsgtwclient.service

import android.R
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.achaquisse.smsgtwclient.MainActivity
import com.achaquisse.smsgtwclient.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlin.coroutines.resume

class GatewayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var sessionManager: SessionManager
    private var pollingJob: Job? = null

    companion object {
        private const val TAG = "GatewayService"
        private const val CHANNEL_ID = "sms_gateway"
        private const val NOTIFICATION_ID = 1
        private const val POLLING_INTERVAL = 20000L // 20 seconds
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        dataStoreManager = DataStoreManager(this)
        sessionManager = SessionManager.getInstance(this)
        createNotificationChannel()
        startForegroundService()
        
        observePollingState()
    }

    private fun observePollingState() {
        serviceScope.launch {
            dataStoreManager.pollingEnabled.collectLatest { enabled ->
                Log.d(TAG, "Polling enabled state changed: $enabled")
                if (enabled) {
                    startPollingLoop()
                } else {
                    stopPollingLoop()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SMS Gateway Active")
            .setContentText("Service is running in background")
            .setSmallIcon(R.drawable.stat_notify_chat)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0, Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "SMS Gateway Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keep the SMS Gateway polling for messages"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun startPollingLoop() {
        if (pollingJob?.isActive == true) {
            Log.d(TAG, "Polling loop already running")
            return
        }
        
        Log.d(TAG, "Starting polling loop")
        pollingJob = serviceScope.launch {
            while (isActive) {
                try {
                    val baseUrl = dataStoreManager.baseUrl.first()
                    val deviceKey = dataStoreManager.deviceKey.first()

                    if (!baseUrl.isNullOrBlank() && !deviceKey.isNullOrBlank()) {
                        Log.d(TAG, "Polling messages from $baseUrl")
                        val api = NetworkModule.getApi(baseUrl, sessionManager)
                        val response = api.pollMessages()
                        
                        dataStoreManager.updateLastPollTime(System.currentTimeMillis())

                        if (response.messages.isNotEmpty()) {
                            Log.d(TAG, "Received ${response.messages.size} messages")
                            for (message in response.messages) {
                                try {
                                    val result = sendSms(message.toNumber, message.body)
                                    if (result.success) {
                                        Log.d(TAG, "Message ${message.id} sent successfully")
                                        api.updateStatus(message.id, StatusUpdateRequest("sent"))
                                    } else {
                                        Log.e(TAG, "Failed to send message ${message.id}: ${result.error}")
                                        api.updateStatus(message.id, StatusUpdateRequest("failed", result.error))
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error processing message ${message.id}: ${e.message}", e)
                                }
                            }
                        }
                    } else {
                        Log.w(TAG, "Base URL or Device Key not configured")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during polling: ${e.message}", e)
                }
                
                delay(POLLING_INTERVAL)
            }
        }
    }

    private fun stopPollingLoop() {
        Log.d(TAG, "Stopping polling loop")
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun sendSms(phoneNumber: String, message: String): SmsResult = withContext(Dispatchers.Main) {
        try {
            withTimeout(60000L) { // 1 minute timeout
                suspendCancellableCoroutine { continuation ->
                    val sentAction = "SMS_SENT_${System.currentTimeMillis()}_${(0..1000).random()}"
                    val sentIntent = PendingIntent.getBroadcast(
                        this@GatewayService, 0, Intent(sentAction), PendingIntent.FLAG_IMMUTABLE
                    )

                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            try {
                                context?.unregisterReceiver(this)
                            } catch (e: Exception) { }
                            
                            val result = if (resultCode == Activity.RESULT_OK) {
                                SmsResult(true)
                            } else {
                                val error = when (resultCode) {
                                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "Generic failure"
                                    SmsManager.RESULT_ERROR_NO_SERVICE -> "No service"
                                    SmsManager.RESULT_ERROR_NULL_PDU -> "Null PDU"
                                    SmsManager.RESULT_ERROR_RADIO_OFF -> "Radio off"
                                    else -> "Unknown error: $resultCode"
                                }
                                SmsResult(false, error)
                            }
                            if (continuation.isActive) {
                                continuation.resume(result)
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        registerReceiver(receiver, IntentFilter(sentAction), RECEIVER_EXPORTED)
                    } else {
                        registerReceiver(receiver, IntentFilter(sentAction))
                    }

                    try {
                        val smsManager = getSystemService(SmsManager::class.java)
                        smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, null)
                    } catch (e: Exception) {
                        try {
                            this@GatewayService.unregisterReceiver(receiver)
                        } catch (unregEx: Exception) { }
                        if (continuation.isActive) {
                            continuation.resume(SmsResult(false, e.message))
                        }
                    }

                    continuation.invokeOnCancellation {
                        try {
                            this@GatewayService.unregisterReceiver(receiver)
                        } catch (e: Exception) { }
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            SmsResult(false, "Timeout waiting for SMS confirmation")
        } catch (e: Exception) {
            SmsResult(false, e.message ?: "Unknown error")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    data class SmsResult(val success: Boolean, val error: String? = null)
}
